package com.marvelspectrum.shared.presentation.components

/**
 * Enum representing the poster/database fetch mode for the viewing catalog
 */
enum class MediaScanMode {
    /**
     * Blacklist mode: Exclude specified titles/catalog folders, include everything else
     */
    BLACKLIST,
    
    /**
     * Whitelist mode: Include only specified titles/catalog folders, exclude everything else
     */
    WHITELIST
}
