package com.marvelspectrum.infrastructure.widget.glance

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Glance Widget Receiver for Marvel Spectrum Widget
 * 
 * This receiver handles widget lifecycle events and updates
 */
class RhythmWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RhythmMusicWidget()
}
