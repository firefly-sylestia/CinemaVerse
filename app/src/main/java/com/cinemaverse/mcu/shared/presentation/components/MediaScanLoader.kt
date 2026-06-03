package com.cinemaverse.mcu.shared.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cinemaverse.mcu.features.local.presentation.viewmodel.MusicViewModel
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumArtworkBackdrop
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPanel
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPrimaryButton
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumProgressRing
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.ProvideSpectrumRhythmTheme
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.SpectrumMusicUniverse
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.rememberSpectrumReachMode
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.spectrumReachMode
import kotlinx.coroutines.delay

@Composable
fun MediaScanLoader(
    musicViewModel: MusicViewModel = viewModel(),
    onScanComplete: () -> Unit
) {
    val songs by musicViewModel.filteredSongs.collectAsState()
    val albums by musicViewModel.filteredAlbums.collectAsState()
    val artists by musicViewModel.filteredArtists.collectAsState()
    val scanProgress by musicViewModel.scanProgress.collectAsState()
    var displayProgress by remember { mutableStateOf(0.08f) }
    var currentStep by remember { mutableStateOf("Starting music scan") }
    var complete by remember { mutableStateOf(false) }
    val reachMode = rememberSpectrumReachMode()

    LaunchedEffect(Unit) { musicViewModel.refreshLibrary() }
    LaunchedEffect(scanProgress, songs.size, albums.size, artists.size) {
        when (scanProgress.stage) {
            "Idle" -> { displayProgress = 0.08f; currentStep = "Starting music scan" }
            "Songs" -> { displayProgress = if (scanProgress.total > 0) (scanProgress.current.toFloat() / scanProgress.total).coerceIn(.12f, .52f) else .22f; currentStep = "Reading audio files • ${scanProgress.current} of ${scanProgress.total}" }
            "Incremental" -> { displayProgress = .55f + if (scanProgress.total > 0) (scanProgress.current.toFloat() / scanProgress.total * .28f) else .08f; currentStep = "Indexing albums, artists, playlists, and artwork" }
            "Complete" -> { displayProgress = 1f; currentStep = "Music spectrum complete"; delay(700); if (!complete) { complete = true; onScanComplete() } }
            "Error" -> { displayProgress = .72f; currentStep = "Scan recovery active — using indexed music"; delay(1200); if (!complete) { complete = true; onScanComplete() } }
            else -> { displayProgress = .35f; currentStep = scanProgress.stage.ifBlank { "Building music library" } }
        }
    }

    ProvideSpectrumRhythmTheme(universe = SpectrumMusicUniverse.Archive) {
        SpectrumArtworkBackdrop(null, Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(22.dp).spectrumReachMode(reachMode),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpectrumPanel(Modifier.fillMaxWidth(), contentPadding = PaddingValues(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("Scanning Your Music Spectrum", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        Text(currentStep, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        SpectrumProgressRing(displayProgress)
                        LinearProgressIndicator(progress = { displayProgress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ScanStat("Songs", songs.size.toString(), Modifier.weight(1f))
                            ScanStat("Albums", albums.size.toString(), Modifier.weight(1f))
                            ScanStat("Artists", artists.size.toString(), Modifier.weight(1f))
                        }
                        SpectrumPrimaryButton("Continue in background", RhythmIcons.Navigation.Library, onScanComplete, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanStat(label: String, value: String, modifier: Modifier = Modifier) {
    SpectrumPanel(modifier, contentPadding = PaddingValues(12.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}
