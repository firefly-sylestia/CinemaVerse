package com.cinemaverse.mcu.shared.data.viewing

import android.net.Uri
import androidx.core.net.toUri
import com.cinemaverse.mcu.features.local.data.database.entity.MCUTitleEntity
import com.cinemaverse.mcu.shared.data.model.Album
import com.cinemaverse.mcu.shared.data.model.Artist
import com.cinemaverse.mcu.shared.data.model.Playlist
import com.cinemaverse.mcu.shared.data.model.Song

object ViewingMusicMapper {
    private const val ASSET_POSTER_PREFIX = "file:///android_asset/mcu_posters/"
    private const val VIEWING_URI_PREFIX = "viewing://mcu/"

    fun itemToSong(item: ViewingItem): Song = Song(
        id = item.id,
        title = item.title,
        artist = item.saga ?: item.phase ?: item.franchise ?: item.studio ?: "Marvel Studios",
        album = item.phase ?: item.saga ?: item.franchise ?: item.category ?: "MCU Viewing",
        albumId = stableId(item.phase ?: item.saga ?: item.franchise ?: "mcu-viewing"),
        duration = parseRuntimeMillis(item.runtime),
        uri = "$VIEWING_URI_PREFIX${item.id}".toUri(),
        artworkUri = resolveArtworkUri(item),
        trackNumber = item.releaseOrder ?: item.chronologicalOrder ?: item.phaseOrder ?: 0,
        year = item.year?.toIntOrNull() ?: item.releaseDate?.take(4)?.toIntOrNull() ?: 0,
        genre = item.genres.joinToString(", ").ifBlank { item.type.name.lowercase().replaceFirstChar { char -> char.titlecase() } },
        dateAdded = releaseDateSortableMillis(item.releaseDate),
        dateModified = System.currentTimeMillis(),
        albumArtist = item.studio ?: item.universe ?: "Marvel Studios"
    )

    fun listToPlaylist(list: ViewingList): Playlist {
        val songs = list.items.map(::itemToSong)
        return Playlist(
            id = list.id,
            name = list.title,
            songs = songs,
            artworkUri = list.localPoster?.let(::assetPosterUri) ?: list.poster?.toUri() ?: songs.firstOrNull { it.artworkUri != null }?.artworkUri
        )
    }

    fun phaseAlbums(items: List<ViewingItem>): List<Album> = groupedAlbums(items, { it.phase }, "MCU Phase")

    fun sagaAlbums(items: List<ViewingItem>): List<Album> = groupedAlbums(items, { it.saga }, "MCU Saga")

    fun franchiseArtists(items: List<ViewingItem>): List<Artist> = items
        .groupBy { it.franchise ?: it.studio ?: it.saga ?: "Marvel Studios" }
        .toSortedMap()
        .map { (name, groupItems) ->
            val songs = groupItems.sortedWith(compareBy<ViewingItem> { it.releaseOrder ?: Int.MAX_VALUE }.thenBy { it.title }).map(::itemToSong)
            Artist(
                id = stableId(name),
                name = name,
                artworkUri = songs.firstOrNull { it.artworkUri != null }?.artworkUri,
                albums = groupedAlbums(groupItems, { it.phase }, name),
                songs = songs,
                numberOfAlbums = groupItems.mapNotNull { it.phase }.distinct().size.coerceAtLeast(1),
                numberOfTracks = songs.size
            )
        }

    fun entityToViewingItem(entity: MCUTitleEntity): ViewingItem = ViewingItem(
        id = entity.id,
        title = entity.title,
        universe = "MCU",
        franchise = entity.series,
        studio = "Marvel Studios",
        type = entity.type.toViewingType(),
        phase = entity.phase,
        saga = entity.saga,
        releaseDate = entity.releaseDate.takeIf { it > 0 }?.let(::formatDateFromMillis),
        runtime = entity.runtime,
        genres = entity.genres?.split('|')?.filter { it.isNotBlank() }.orEmpty(),
        overview = entity.overview,
        localPoster = entity.posterPath,
        trailerUrl = entity.trailerUrl,
        youtubeVideoId = entity.youtubeVideoId,
        trailerSource = entity.youtubeVideoId?.let { TrailerSource.YOUTUBE },
        releaseOrder = entity.viewingOrder,
        chronologicalOrder = entity.viewingOrder,
        watched = entity.watched,
        favorite = entity.favorite,
        watchlisted = entity.watchlisted,
        notes = entity.notes
    )

    fun itemToEntity(item: ViewingItem, existing: MCUTitleEntity? = null): MCUTitleEntity = MCUTitleEntity(
        id = item.id,
        title = item.title,
        type = item.type.name.lowercase(),
        series = item.franchise ?: item.category ?: item.saga ?: "MCU",
        viewingOrder = item.chronologicalOrder ?: item.releaseOrder ?: item.phaseOrder ?: 0,
        releaseDate = releaseDateSortableMillis(item.releaseDate),
        posterPath = item.localPoster ?: item.poster?.substringAfterLast('/'),
        watched = existing?.watched ?: item.watched,
        watchedDate = existing?.watchedDate,
        dateAdded = existing?.dateAdded ?: System.currentTimeMillis(),
        dateModified = System.currentTimeMillis(),
        favorite = existing?.favorite ?: item.favorite,
        watchlisted = existing?.watchlisted ?: item.watchlisted,
        notes = existing?.notes ?: item.notes,
        userRating = existing?.userRating ?: 0,
        openedCount = existing?.openedCount ?: 0,
        lastOpenedDate = existing?.lastOpenedDate,
        saga = item.saga,
        phase = item.phase,
        genres = item.genres.joinToString("|"),
        runtime = item.runtime,
        trailerUrl = item.trailerUrl,
        youtubeVideoId = item.youtubeVideoId,
        overview = item.overview ?: item.description ?: item.plot
    )

    private fun groupedAlbums(items: List<ViewingItem>, keySelector: (ViewingItem) -> String?, fallback: String): List<Album> = items
        .groupBy { keySelector(it) ?: fallback }
        .toSortedMap()
        .map { (title, groupItems) ->
            val songs = groupItems.sortedWith(compareBy<ViewingItem> { it.phaseOrder ?: it.releaseOrder ?: Int.MAX_VALUE }.thenBy { it.title }).map(::itemToSong)
            Album(
                id = stableId(title),
                title = title,
                artist = groupItems.firstOrNull()?.saga ?: groupItems.firstOrNull()?.studio ?: "Marvel Studios",
                artworkUri = songs.firstOrNull { it.artworkUri != null }?.artworkUri,
                year = songs.map { it.year }.filter { it > 0 }.minOrNull() ?: 0,
                songs = songs,
                numberOfSongs = songs.size
            )
        }

    private fun resolveArtworkUri(item: ViewingItem): Uri? = item.localPoster?.let(::assetPosterUri)
        ?: item.tmdbPoster?.toUri()
        ?: item.poster?.toUri()
        ?: item.omdbPoster?.toUri()

    private fun assetPosterUri(path: String): Uri = if (path.startsWith("file://") || path.startsWith("content://")) {
        path.toUri()
    } else {
        "$ASSET_POSTER_PREFIX${path.substringAfterLast('/') }".toUri()
    }

    private fun parseRuntimeMillis(runtime: String?): Long {
        val minutes = runtime?.let { Regex("(\\d+)").find(it)?.groupValues?.getOrNull(1)?.toLongOrNull() } ?: 0L
        return minutes * 60_000L
    }

    private fun releaseDateSortableMillis(date: String?): Long = runCatching {
        if (date.isNullOrBlank()) 0L else java.time.LocalDate.parse(date).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
    }.getOrDefault(0L)

    private fun formatDateFromMillis(millis: Long): String = java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneOffset.UTC)
        .toLocalDate()
        .toString()

    private fun stableId(value: String): String = value.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    private fun String.toViewingType(): ViewingType = when (uppercase()) {
        "SERIES", "TV", "SHOW" -> ViewingType.SERIES
        "EPISODE" -> ViewingType.EPISODE
        "SPECIAL" -> ViewingType.SPECIAL
        "SHORT" -> ViewingType.SHORT
        "ONE_SHOT", "ONESHOT", "ONE-SHOT" -> ViewingType.ONE_SHOT
        else -> ViewingType.MOVIE
    }
}
