package chromahub.rhythm.app.infrastructure.audio

import android.content.Context
import android.media.AudioFormat
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.AudioCapabilities
import androidx.media3.common.AudioAttributes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.AuxEffectInfo
import java.nio.ByteBuffer

/**
 * Factory for creating AudioSink instances configured for bit-perfect playback.
 * 
 * Bit-perfect playback means outputting audio at its native sample rate without resampling.
 * Android normally resamples all audio to 48kHz, but this factory configures AudioSink to use
 * the track's original sample rate (e.g., 44.1kHz for CD quality, 96kHz for Hi-Res).
 * 
 * This prevents quality loss from unnecessary resampling and provides the best possible audio quality.
 */
@OptIn(UnstableApi::class)
object BitPerfectAudioSink {
    
    private const val TAG = "BitPerfectAudioSink"
    
    // Rhythm audio processors
    private var rhythmBassBoostProcessor: RhythmBassBoostProcessor? = null
    private var rhythmSpatializationProcessor: RhythmSpatializationProcessor? = null
    
    
    /**
     * Create AudioSink with optional bit-perfect configuration and Rhythm audio effects.
     *
     * Bit-perfect mode:
     * - Enables float output so ExoPlayer preserves the native sample format
     *   instead of down-converting everything to 16-bit PCM.
     * - Strips ALL audio processors (bass boost, spatialization, etc.) so the
     *   decoded stream reaches the AudioTrack unmodified.
     *
     * NOTE: True bit-perfect playback on Android also requires bypassing the
     * platform audio mixer, which needs MIXER_BEHAVIOR_BIT_PERFECT (Android 14+)
     * or vendor-specific direct AudioTrack paths. This factory handles the
     * ExoPlayer side; the mixer bypass is a platform-level concern.
     */
    fun create(
        context: Context, 
        enableBitPerfect: Boolean,
        bassBoostProcessor: RhythmBassBoostProcessor? = null,
        spatializationProcessor: RhythmSpatializationProcessor? = null
    ): AudioSink {
        Log.d(TAG, "Creating RhythmBitPerfectAudioSink wrapper (bit-perfect: $enableBitPerfect, Rhythm effects: ${bassBoostProcessor != null || spatializationProcessor != null})")
        
        // Store processor references (available for later queries even if bit-perfect skips them)
        rhythmBassBoostProcessor = bassBoostProcessor
        rhythmSpatializationProcessor = spatializationProcessor
        
        // Create local child instances parented to the shared ones for thread safety (crossfades)
        val localBassBoost = bassBoostProcessor?.let { parent ->
            RhythmBassBoostProcessor().apply { setParent(parent) }
        }
        val localSpatialization = spatializationProcessor?.let { parent ->
            RhythmSpatializationProcessor().apply { setParent(parent) }
        }
        
        return RhythmBitPerfectAudioSink(
            context = context,
            enableBitPerfect = enableBitPerfect,
            bassBoostProcessor = localBassBoost,
            spatializationProcessor = localSpatialization
        )
    }
    
    /**
     * Get the current bass boost processor
     */
    fun getBassBoostProcessor(): RhythmBassBoostProcessor? = rhythmBassBoostProcessor
    
    /**
     * Get the current spatialization processor
     */
    fun getSpatializationProcessor(): RhythmSpatializationProcessor? = rhythmSpatializationProcessor
    
    /**
     * Check if the device supports the requested sample rate
     */
    fun isSampleRateSupported(sampleRate: Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Before Android M, limited sample rate support
            return sampleRate in listOf(44100, 48000)
        }
        
        // Android M and above support a wider range
        val supported = sampleRate in listOf(44100, 48000, 88200, 96000, 176400, 192000)
        Log.d(TAG, "Sample rate ${sampleRate}Hz supported: $supported")
        return supported
    }
    
    /**
     * Log the current playback format for debugging
     */
    fun logPlaybackFormat(format: Format) {
        val sampleRate = if (format.sampleRate != Format.NO_VALUE) format.sampleRate else "unknown"
        val channels = if (format.channelCount != Format.NO_VALUE) format.channelCount else "unknown"
        val bitDepth = when (format.pcmEncoding) {
            C.ENCODING_PCM_8BIT -> "8-bit"
            C.ENCODING_PCM_16BIT -> "16-bit"
            C.ENCODING_PCM_24BIT -> "24-bit"
            C.ENCODING_PCM_32BIT -> "32-bit"
            C.ENCODING_PCM_FLOAT -> "32-bit float"
            else -> "unknown"
        }
        
        Log.i(TAG, "=== Bit-Perfect Playback ===")
        Log.i(TAG, "Sample Rate: ${sampleRate}Hz")
        Log.i(TAG, "Channels: $channels")
        Log.i(TAG, "Bit Depth: $bitDepth")
        Log.i(TAG, "Codec: ${format.sampleMimeType ?: "unknown"}")
        Log.i(TAG, "==========================")
    }
    
    /**
     * Get channel mask for the specified channel count
     */
    fun getChannelMask(channelCount: Int): Int {
        return when (channelCount) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            6 -> AudioFormat.CHANNEL_OUT_5POINT1
            8 -> AudioFormat.CHANNEL_OUT_7POINT1_SURROUND
            else -> AudioFormat.CHANNEL_OUT_STEREO
        }
    }
}

@OptIn(UnstableApi::class)
private class RhythmBitPerfectAudioSink(
    context: Context,
    private val enableBitPerfect: Boolean,
    bassBoostProcessor: RhythmBassBoostProcessor?,
    spatializationProcessor: RhythmSpatializationProcessor?
) : AudioSink {

    private val standardSink: DefaultAudioSink
    private val floatSink: DefaultAudioSink
    private var activeSink: AudioSink

    init {
        val standardBuilder = DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(false)
        
        val processors = mutableListOf<AudioProcessor>()
        if (bassBoostProcessor != null) {
            processors.add(bassBoostProcessor)
        }
        if (spatializationProcessor != null) {
            processors.add(spatializationProcessor)
        }
        if (processors.isNotEmpty()) {
            standardBuilder.setAudioProcessorChain(
                DefaultAudioSink.DefaultAudioProcessorChain(*processors.toTypedArray())
            )
        }
        standardSink = standardBuilder.build()

        floatSink = DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(true)
            .build()

        activeSink = standardSink
    }

    private fun getSinkForFormat(format: Format): AudioSink {
        if (!enableBitPerfect) {
            return standardSink
        }
        val isHighRes = format.pcmEncoding == C.ENCODING_PCM_24BIT ||
                        format.pcmEncoding == C.ENCODING_PCM_32BIT ||
                        format.pcmEncoding == C.ENCODING_PCM_FLOAT
        return if (isHighRes) floatSink else standardSink
    }

    override fun setListener(listener: AudioSink.Listener) {
        standardSink.setListener(listener)
        floatSink.setListener(listener)
    }

    override fun supportsFormat(format: Format): Boolean {
        return getSinkForFormat(format).supportsFormat(format)
    }

    override fun getFormatSupport(format: Format): Int {
        return getSinkForFormat(format).getFormatSupport(format)
    }

    override fun getCurrentPositionUs(sourceEnded: Boolean): Long {
        return activeSink.getCurrentPositionUs(sourceEnded)
    }

    override fun configure(inputFormat: Format, inputSize: Int, outputChannels: IntArray?) {
        val newSink = getSinkForFormat(inputFormat)
        if (newSink != activeSink) {
            Log.d("RhythmBitPerfectSink", "Switching active AudioSink to ${if (newSink == floatSink) "floatSink (Hi-Res)" else "standardSink"}")
            activeSink.flush()
            activeSink.reset()
            activeSink = newSink
        }
        activeSink.configure(inputFormat, inputSize, outputChannels)
    }

    override fun play() {
        activeSink.play()
    }

    override fun handleDiscontinuity() {
        activeSink.handleDiscontinuity()
    }

    override fun handleBuffer(
        buffer: ByteBuffer,
        presentationTimeUs: Long,
        encodedAccessUnitCount: Int
    ): Boolean {
        return activeSink.handleBuffer(buffer, presentationTimeUs, encodedAccessUnitCount)
    }

    override fun playToEndOfStream() {
        activeSink.playToEndOfStream()
    }

    override fun isEnded(): Boolean {
        return activeSink.isEnded()
    }

    override fun hasPendingData(): Boolean {
        return activeSink.hasPendingData()
    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        standardSink.setPlaybackParameters(playbackParameters)
        floatSink.setPlaybackParameters(playbackParameters)
    }

    override fun getPlaybackParameters(): PlaybackParameters {
        return activeSink.getPlaybackParameters()
    }

    override fun setSkipSilenceEnabled(skipSilenceEnabled: Boolean) {
        standardSink.setSkipSilenceEnabled(skipSilenceEnabled)
        floatSink.setSkipSilenceEnabled(skipSilenceEnabled)
    }

    override fun getSkipSilenceEnabled(): Boolean {
        return activeSink.getSkipSilenceEnabled()
    }

    override fun setAudioAttributes(audioAttributes: AudioAttributes) {
        standardSink.setAudioAttributes(audioAttributes)
        floatSink.setAudioAttributes(audioAttributes)
    }

    override fun getAudioAttributes(): AudioAttributes? {
        return activeSink.getAudioAttributes()
    }

    override fun setAudioSessionId(audioSessionId: Int) {
        standardSink.setAudioSessionId(audioSessionId)
        floatSink.setAudioSessionId(audioSessionId)
    }

    override fun setAuxEffectInfo(auxEffectInfo: AuxEffectInfo) {
        standardSink.setAuxEffectInfo(auxEffectInfo)
        floatSink.setAuxEffectInfo(auxEffectInfo)
    }

    override fun getAudioTrackBufferSizeUs(): Long {
        return activeSink.getAudioTrackBufferSizeUs()
    }

    override fun enableTunnelingV21() {
        standardSink.enableTunnelingV21()
        floatSink.enableTunnelingV21()
    }

    override fun disableTunneling() {
        standardSink.disableTunneling()
        floatSink.disableTunneling()
    }

    override fun setVolume(volume: Float) {
        standardSink.setVolume(volume)
        floatSink.setVolume(volume)
    }

    override fun pause() {
        activeSink.pause()
    }

    override fun flush() {
        activeSink.flush()
    }

    override fun reset() {
        standardSink.reset()
        floatSink.reset()
    }
}
