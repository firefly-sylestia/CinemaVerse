package com.marvelspectrum.features.local.domain.recommendation

import com.marvelspectrum.shared.data.model.Song
import java.util.Locale

data class TrackFeatureVector(
    val songId: Long,
    val titleTokens: Set<String>,
    val artistTokens: Set<String>,
    val albumArtistTokens: Set<String>,
    val albumTokens: Set<String>,
    val genreTokens: Set<String>,
    val language: TrackLanguage,
    val languageConfidence: Float = 0.5f,
    val year: Int?,
    val decade: Int?,
    val durationBucket: DurationBucket,
    val bitrateBucket: AudioQualityBucket?,
    val isFavorite: Boolean,
    val playCount: Int,
    val skipCount: Int,
    val lastPlayedAt: Long?,
    val lastSkippedAt: Long? = null,
    val addedAt: Long?,
    val folderTokens: Set<String>,
    val playlistIds: Set<Long>
)

enum class TrackLanguage { ENGLISH, HINDI, HINDI_TRANSLITERATED, MIXED, INSTRUMENTAL, UNKNOWN, OTHER }
enum class LanguageSource { EXPLICIT_METADATA, USER_OVERRIDE, SCRIPT, KEYWORD_HEURISTIC, ONLINE_METADATA, UNKNOWN }
data class LanguagePrediction(val language: TrackLanguage, val confidence: Float, val source: LanguageSource)
enum class DurationBucket { SHORT, MEDIUM, LONG, EPIC }
enum class AudioQualityBucket { LOW, STANDARD, HIGH, LOSSLESS }

data class CachedTrackFeatures(
    val songId: Long,
    val featureVersion: Int,
    val language: TrackLanguage,
    val languageConfidence: Float,
    val normalizedGenreTokens: String,
    val normalizedArtistTokens: String,
    val normalizedAlbumTokens: String,
    val updatedAt: Long
) {
    companion object { const val CURRENT_FEATURE_VERSION = 1 }
}

object TrackFeatureFactory {
    fun fromSong(
        song: Song,
        isFavorite: Boolean = false,
        playCount: Int = 0,
        skipCount: Int = 0,
        lastPlayedAt: Long? = null,
        lastSkippedAt: Long? = null,
        playlistIds: Set<Long> = emptySet()
    ): TrackFeatureVector {
        val languagePrediction = LanguagePreferenceModel.predict(song)
        val year = song.year.takeIf { it > 0 }
        return TrackFeatureVector(
            songId = song.id.toLongOrNull() ?: stableLongId(song.id),
            titleTokens = tokenize(song.title),
            artistTokens = tokenize(song.artist),
            albumArtistTokens = tokenize(song.albumArtist ?: song.artist),
            albumTokens = tokenize(song.album),
            genreTokens = tokenize(song.genre.orEmpty()),
            language = languagePrediction.language,
            languageConfidence = languagePrediction.confidence,
            year = year,
            decade = year?.let { (it / 10) * 10 },
            durationBucket = durationBucket(song.duration),
            bitrateBucket = song.bitrate?.let(::bitrateBucket),
            isFavorite = isFavorite,
            playCount = playCount,
            skipCount = skipCount,
            lastPlayedAt = lastPlayedAt,
            lastSkippedAt = lastSkippedAt,
            addedAt = song.dateAdded.takeIf { it > 0 },
            folderTokens = tokenize(song.uri.path.orEmpty()).takeLastTokens(4),
            playlistIds = playlistIds
        )
    }

    fun tokenize(value: String): Set<String> = value
        .lowercase(Locale.ROOT)
        .split(Regex("[^\\p{L}\\p{Nd}]+"))
        .map { it.trim() }
        .filter { it.length >= 2 }
        .toSet()

    private fun Set<String>.takeLastTokens(count: Int): Set<String> = toList().takeLast(count).toSet()

    private fun durationBucket(durationMs: Long): DurationBucket = when {
        durationMs < 150_000 -> DurationBucket.SHORT
        durationMs < 330_000 -> DurationBucket.MEDIUM
        durationMs < 600_000 -> DurationBucket.LONG
        else -> DurationBucket.EPIC
    }

    private fun bitrateBucket(bitrate: Int): AudioQualityBucket = when {
        bitrate < 160_000 -> AudioQualityBucket.LOW
        bitrate < 256_000 -> AudioQualityBucket.STANDARD
        bitrate < 700_000 -> AudioQualityBucket.HIGH
        else -> AudioQualityBucket.LOSSLESS
    }

    private fun stableLongId(value: String): Long = value.fold(1125899906842597L) { acc, char -> acc * 31 + char.code }
}
