package com.cinemaverse.mcu.features.local.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cinemaverse.mcu.features.local.presentation.screens.onboarding.OnboardingStep
import com.cinemaverse.mcu.features.local.presentation.screens.onboarding.PermissionScreenState
import com.cinemaverse.mcu.features.local.presentation.viewmodel.MusicViewModel
import com.cinemaverse.mcu.features.streaming.presentation.viewmodel.StreamingMusicViewModel
import com.cinemaverse.mcu.shared.data.model.AppSettings
import com.cinemaverse.mcu.shared.presentation.components.icons.MaterialSymbolIcon
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumArtworkBackdrop
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPanel
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPrimaryButton
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumProgressRing
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.ProvideSpectrumRhythmTheme
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.SpectrumMusicUniverse
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.rememberSpectrumReachMode
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.spectrumReachMode
import com.cinemaverse.mcu.shared.presentation.viewmodel.AppUpdaterViewModel
import com.cinemaverse.mcu.shared.presentation.viewmodel.ThemeViewModel

@Composable
fun OnboardingScreen(
    currentStep: OnboardingStep,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onContinueFullTour: () -> Unit,
    onSkipFullTour: () -> Unit,
    onRequestAgain: () -> Unit,
    permissionScreenState: PermissionScreenState,
    isParentLoading: Boolean,
    themeViewModel: ThemeViewModel,
    appSettings: AppSettings,
    musicViewModel: MusicViewModel,
    updaterViewModel: AppUpdaterViewModel = viewModel(),
    streamingViewModel: StreamingMusicViewModel = viewModel(),
    onFinish: () -> Unit = {}
) {
    val steps = listOf(
        OnboardingStep.WELCOME,
        OnboardingStep.APP_MODE_CHOICE,
        OnboardingStep.PERMISSIONS,
        OnboardingStep.BACKUP_RESTORE,
        OnboardingStep.AUDIO_PLAYBACK,
        OnboardingStep.MEDIA_SCAN,
        OnboardingStep.THEMING,
        OnboardingStep.PLAYER_THEME_CHOICE,
        OnboardingStep.RHYTHM_GUARD,
        OnboardingStep.GESTURES,
        OnboardingStep.WIDGETS,
        OnboardingStep.INTEGRATIONS,
        OnboardingStep.RHYTHM_STATS,
        OnboardingStep.SETUP_FINISHED,
        OnboardingStep.COMPLETE
    )
    val index = steps.indexOf(currentStep).takeIf { it >= 0 } ?: 0
    val progress = ((index + 1).toFloat() / steps.size.toFloat()).coerceIn(0f, 1f)
    val copy = onboardingCopy(currentStep, permissionScreenState)
    val reachMode = rememberSpectrumReachMode()

    ProvideSpectrumRhythmTheme(universe = SpectrumMusicUniverse.Reactor) {
        SpectrumArtworkBackdrop(null, Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(22.dp).navigationBarsPadding().spectrumReachMode(reachMode),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(18.dp))
                SpectrumPanel(Modifier.fillMaxWidth(), contentPadding = PaddingValues(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("Initialize Spectrum Rhythm Reactor", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        Text("Step ${index + 1} of ${steps.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        SpectrumProgressRing(progress)
                        Text(copy.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(copy.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        SpectrumPanel(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                            Text(copy.tip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                }
                SpectrumPanel(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SpectrumPrimaryButton("Back", RhythmIcons.Navigation.Back, onPrevStep, Modifier.weight(1f))
                            SpectrumPrimaryButton(if (currentStep == OnboardingStep.COMPLETE || currentStep == OnboardingStep.SETUP_FINISHED) "Finish" else "Next", copy.icon, if (currentStep == OnboardingStep.COMPLETE || currentStep == OnboardingStep.SETUP_FINISHED) onFinish else onNextStep, Modifier.weight(1f))
                        }
                        if (currentStep == OnboardingStep.PERMISSIONS) SpectrumPrimaryButton("Request permission", RhythmIcons.Actions.Check, onRequestAgain, Modifier.fillMaxWidth())
                        if (currentStep == OnboardingStep.FULL_TOUR_PROMPT) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { SpectrumPrimaryButton("Full tour", RhythmIcons.Actions.Info, onContinueFullTour, Modifier.weight(1f)); SpectrumPrimaryButton("Skip", RhythmIcons.Navigation.Forward, onSkipFullTour, Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

private data class OnboardingSpectrumCopy(val title: String, val body: String, val tip: String, val icon: MaterialSymbolIcon)

private fun onboardingCopy(step: OnboardingStep, permissionState: PermissionScreenState): OnboardingSpectrumCopy = when (step) {
    OnboardingStep.WELCOME -> OnboardingSpectrumCopy("Welcome to Marvel Spectrum", "A music-first command center for local songs, playlists, lyrics, queue control, and adaptive player themes.", "Swipe down anywhere on setup to bring the command deck closer to your thumb.", RhythmIcons.Player.Play)
    OnboardingStep.APP_MODE_CHOICE -> OnboardingSpectrumCopy("Choose your rhythm mode", "Keep the app focused on local music or connect streaming services when configured.", "No placeholder services are enabled here; setup only advances existing behavior.", RhythmIcons.Navigation.Library)
    OnboardingStep.STREAMING_SETUP -> OnboardingSpectrumCopy("Connect streaming", "Use your existing service setup to bring remote sessions into the music shell.", "Credentials stay handled by the current streaming logic.", RhythmIcons.Devices.Bluetooth)
    OnboardingStep.PERMISSIONS -> OnboardingSpectrumCopy("Grant music access", "Permission state: ${permissionState.label()}. Audio library access lets Spectrum scan songs and artwork.", "Controls stay in the bottom command zone for one-hand setup.", RhythmIcons.Actions.Check)
    OnboardingStep.BACKUP_RESTORE -> OnboardingSpectrumCopy("Backup and restore", "Protect playlists, settings, and rhythm history before you tune the UI.", "Restores use the existing backup flow.", RhythmIcons.Actions.Restore)
    OnboardingStep.AUDIO_PLAYBACK -> OnboardingSpectrumCopy("Tune playback", "Initialize playback controls, device output, queue actions, lyrics, sleep timer, and EQ preferences.", "The redesigned console preserves playback behavior.", RhythmIcons.Player.Equalizer)
    OnboardingStep.THEMING -> OnboardingSpectrumCopy("Adaptive theme", "Enable Material You, dark/light/system support, and album-art-inspired Spectrum accents.", "Neon is clamped for readability in light mode.", RhythmIcons.Actions.Tune)
    OnboardingStep.PLAYER_THEME_CHOICE -> OnboardingSpectrumCopy("Player console", "Choose the player presentation while the Spectrum Reactor controls remain reachable.", "Artwork influences background, buttons, progress, and edge glow.", RhythmIcons.Player.Play)
    OnboardingStep.RHYTHM_GUARD -> OnboardingSpectrumCopy("Rhythm Guard", "Configure hearing safety and listening comfort without leaving the music setup flow.", "Safety labels remain readable and TalkBack-friendly.", RhythmIcons.Actions.Info)
    OnboardingStep.FULL_TOUR_PROMPT -> OnboardingSpectrumCopy("Take the full tour?", "Explore widgets, gestures, integrations, stats, and customization, or skip directly to the app.", "Either path preserves setup state.", RhythmIcons.Navigation.Forward)
    OnboardingStep.NOTIFICATIONS -> OnboardingSpectrumCopy("Notifications", "Configure playback notifications for active music control.", "Notification actions mirror the mini-player.", RhythmIcons.Actions.Info)
    OnboardingStep.UPDATER -> OnboardingSpectrumCopy("Updater", "Keep update preferences aligned with your distribution channel.", "No movie catalog settings are changed here.", RhythmIcons.Actions.Update)
    OnboardingStep.LIBRARY_SETUP, OnboardingStep.MEDIA_SCAN -> OnboardingSpectrumCopy("Music library setup", "Scan audio files, index albums and artists, build playlists, and extract artwork colors.", "The scan screen shows live progress and recovery messaging.", RhythmIcons.Actions.Refresh)
    OnboardingStep.GESTURES -> OnboardingSpectrumCopy("One-hand gestures", "Swipe down to enter reach mode, swipe up or navigate away to restore the full command layout.", "Motion stays subtle at 180–280 ms.", RhythmIcons.Navigation.KeyboardArrowDown)
    OnboardingStep.WIDGETS -> OnboardingSpectrumCopy("Widgets", "Set up music widgets that match the redesigned Spectrum playback surfaces.", "Widgets continue using existing update behavior.", RhythmIcons.Navigation.Home)
    OnboardingStep.INTEGRATIONS -> OnboardingSpectrumCopy("Integrations", "Configure lyric services, scrobbling, API services, and presence integrations where available.", "Only wired behavior is surfaced.", RhythmIcons.Actions.Share)
    OnboardingStep.RHYTHM_STATS -> OnboardingSpectrumCopy("Rhythm stats", "Listening stats become archive-style insights across songs, albums, and artists.", "Stats are music-focused, not viewing-catalog metrics.", RhythmIcons.Player.Equalizer)
    OnboardingStep.SETUP_FINISHED, OnboardingStep.COMPLETE -> OnboardingSpectrumCopy("Reactor initialized", "Your Spectrum Rhythm music experience is ready.", "Open the app and start playback from the bottom command deck.", RhythmIcons.Actions.Check)
}


private fun PermissionScreenState.label(): String = when (this) {
    PermissionScreenState.Loading -> "checking"
    PermissionScreenState.PermissionsRequired -> "required"
    PermissionScreenState.ShowRationale -> "rationale"
    PermissionScreenState.RedirectToSettings -> "settings required"
    PermissionScreenState.PermissionsGranted -> "granted"
}
