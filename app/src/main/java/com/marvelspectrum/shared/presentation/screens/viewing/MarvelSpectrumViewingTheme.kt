package com.marvelspectrum.shared.presentation.screens.viewing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Shared layout rhythm for every cinema/viewing destination. */
object SpectrumSpacing {
    val screenPadding = 20.dp
    val sectionGap = 22.dp
    val cardGap = 14.dp
    val chipGap = 8.dp
    val bottomSafePadding = 120.dp
    val cardContentPadding = 16.dp
}

/** Soft geometry shared by the viewing experience. */
object SpectrumShapes {
    val largeSoftCard: Shape = RoundedCornerShape(28.dp)
    val pillTab: Shape = RoundedCornerShape(50)
    val circularIconButton: Shape = CircleShape
    val posterMask: Shape = RoundedCornerShape(20.dp)
}

/** Purposeful durations: interaction feedback is quick; scene changes have room to breathe. */
object SpectrumMotion {
    const val pressMillis = 160
    const val selectionMillis = 180
    const val carouselMillis = 360
    const val backdropRevealMillis = 420
    const val pressedScale = 0.975f

    fun pressSpec() = tween<Float>(pressMillis, easing = FastOutSlowInEasing)
    fun selectionSpec() = tween<Color>(selectionMillis, easing = FastOutSlowInEasing)
}

data class SpectrumUniverseAccent(
    val primary: Color,
    val secondary: Color,
    val container: Color,
    val onContainer: Color
)

/**
 * Universe accents intentionally blend theme roles instead of returning fixed brand hex values.
 * This keeps the MCU/DC/Cinemaverse identity while preserving the active light/dark color scheme.
 */
@Composable
fun spectrumUniverseAccent(universe: String?): SpectrumUniverseAccent {
    val scheme = MaterialTheme.colorScheme
    return when {
        universe.equals("MCU", ignoreCase = true) || universe.orEmpty().contains("Marvel", ignoreCase = true) ->
            SpectrumUniverseAccent(
                primary = lerp(scheme.error, scheme.primary, 0.28f),
                secondary = lerp(scheme.tertiary, scheme.primary, 0.22f),
                container = lerp(scheme.errorContainer, scheme.primaryContainer, 0.30f),
                onContainer = lerp(scheme.onErrorContainer, scheme.onPrimaryContainer, 0.30f)
            )
        universe.orEmpty().contains("DC", ignoreCase = true) || universe.equals("Elseworlds", ignoreCase = true) ->
            SpectrumUniverseAccent(
                primary = lerp(scheme.primary, scheme.tertiary, 0.22f),
                secondary = lerp(scheme.tertiary, scheme.secondary, 0.30f),
                container = lerp(scheme.primaryContainer, scheme.tertiaryContainer, 0.28f),
                onContainer = lerp(scheme.onPrimaryContainer, scheme.onTertiaryContainer, 0.28f)
            )
        else -> SpectrumUniverseAccent(
            primary = lerp(scheme.primary, scheme.secondary, 0.30f),
            secondary = lerp(scheme.secondary, scheme.tertiary, 0.35f),
            container = lerp(scheme.primaryContainer, scheme.secondaryContainer, 0.35f),
            onContainer = lerp(scheme.onPrimaryContainer, scheme.onSecondaryContainer, 0.35f)
        )
    }
}

@Composable
fun SpectrumGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = SpectrumShapes.largeSoftCard,
    accent: SpectrumUniverseAccent? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val resolvedAccent = accent ?: spectrumUniverseAccent(null)
    Surface(
        modifier = modifier,
        shape = shape,
        color = lerp(scheme.surfaceContainerHigh, resolvedAccent.container, 0.10f),
        contentColor = scheme.onSurface,
        border = BorderStroke(1.dp, lerp(scheme.outlineVariant, resolvedAccent.primary, 0.16f)),
        tonalElevation = 2.dp
    ) {
        Box(content = content)
    }
}

@Composable
fun SpectrumPillTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable RowScope.() -> Unit
) {
    val accent = spectrumUniverseAccent(null)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) SpectrumMotion.pressedScale else 1f, SpectrumMotion.pressSpec(), label = "spectrumTabPress")
    val container by animateColorAsState(
        if (selected) accent.primary else MaterialTheme.colorScheme.surfaceContainer,
        SpectrumMotion.selectionSpec(),
        label = "spectrumTabContainer"
    )
    val contentColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        SpectrumMotion.selectionSpec(),
        label = "spectrumTabContent"
    )
    Surface(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        shape = SpectrumShapes.pillTab,
        color = container,
        contentColor = contentColor,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.clickable(interactionSource = interaction, indication = null, onClick = onClick).padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap),
            verticalAlignment = Alignment.CenterVertically,
            content = label
        )
    }
}

@Composable
fun SpectrumIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) SpectrumMotion.pressedScale else 1f, SpectrumMotion.pressSpec(), label = "spectrumIconPress")
    Surface(
        modifier = modifier.size(44.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        shape = SpectrumShapes.circularIconButton,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        IconButton(onClick = onClick, enabled = enabled, interactionSource = interaction, content = content)
    }
}

@Composable
fun SpectrumSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        action?.invoke()
    }
}

@Composable
fun SpectrumGradientScaffoldBackground(
    modifier: Modifier = Modifier,
    universe: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val accent = spectrumUniverseAccent(universe)
    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(
                    lerp(scheme.background, accent.container, 0.12f),
                    scheme.background,
                    lerp(scheme.background, scheme.surfaceContainer, 0.45f)
                )
            )
        ),
        content = content
    )
}
