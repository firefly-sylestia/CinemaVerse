package com.marvelspectrum.features.local.domain.recommendation

internal fun feature(
    id: Long,
    artist: String = "artist$id",
    genre: String = "pop",
    language: TrackLanguage = TrackLanguage.ENGLISH,
    favorite: Boolean = false,
    playCount: Int = 0,
    skipCount: Int = 0,
    lastPlayedAt: Long? = null,
    playlistIds: Set<Long> = emptySet()
) = TrackFeatureVector(
    songId = id,
    titleTokens = setOf("song$id"),
    artistTokens = setOf(artist),
    albumArtistTokens = setOf(artist),
    albumTokens = setOf("album${id / 10}"),
    genreTokens = setOf(genre),
    language = language,
    languageConfidence = 0.9f,
    year = 2020,
    decade = 2020,
    durationBucket = DurationBucket.MEDIUM,
    bitrateBucket = AudioQualityBucket.STANDARD,
    isFavorite = favorite,
    playCount = playCount,
    skipCount = skipCount,
    lastPlayedAt = lastPlayedAt,
    addedAt = null,
    folderTokens = setOf("music"),
    playlistIds = playlistIds
)
