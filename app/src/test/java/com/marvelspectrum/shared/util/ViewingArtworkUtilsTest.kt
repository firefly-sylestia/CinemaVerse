package com.marvelspectrum.shared.util

import com.marvelspectrum.shared.data.viewing.ViewingItem
import com.marvelspectrum.shared.data.viewing.ViewingList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ViewingArtworkUtilsTest {
    private val localPoster = "file:///android_asset/mcu_posters/local-poster.jpg"
    private val localBackdrop = "file:///android_asset/mcu_posters/local-backdrop.jpg"
    private val tmdbPoster = "/tmdb-poster.jpg"
    private val tmdbBackdrop = "/tmdb-backdrop.jpg"
    private val omdbPoster = "https://images.example.com/omdb-poster.jpg"
    private val remotePoster = "https://images.example.com/remote-poster.jpg"
    private val remoteBackdrop = "https://images.example.com/remote-backdrop.jpg"

    @Test
    fun resolveHeroBackdrop_prefersBackdropSourcesBeforePosterFallbacks() {
        val item = itemWithAllArtwork()

        assertEquals(localBackdrop, ViewingArtworkUtils.resolveHeroBackdrop(item, preferLocalArtwork = true))
        assertEquals("${ViewingArtworkUtils.TMDB_IMAGE_BASE_URL}/w1280/tmdb-backdrop.jpg", ViewingArtworkUtils.resolveHeroBackdrop(item, preferLocalArtwork = false))
        assertEquals(remoteBackdrop, ViewingArtworkUtils.resolveHeroBackdrop(item.copy(tmdbBackdrop = null), preferLocalArtwork = false))
        assertEquals("${ViewingArtworkUtils.TMDB_IMAGE_BASE_URL}/w500/tmdb-poster.jpg", ViewingArtworkUtils.resolveHeroBackdrop(item.copy(tmdbBackdrop = null, backdrop = null), preferLocalArtwork = false))
        assertEquals(omdbPoster, ViewingArtworkUtils.resolveHeroBackdrop(item.copy(tmdbBackdrop = null, backdrop = null, tmdbPoster = null), preferLocalArtwork = false))
    }

    @Test
    fun resolveCardPoster_prefersPosterSourcesBeforeBackdropFallbacks() {
        val item = itemWithAllArtwork()

        assertEquals(localPoster, ViewingArtworkUtils.resolveCardPoster(item, preferLocalArtwork = true))
        assertEquals("${ViewingArtworkUtils.TMDB_IMAGE_BASE_URL}/w500/tmdb-poster.jpg", ViewingArtworkUtils.resolveCardPoster(item, preferLocalArtwork = false))
        assertEquals(omdbPoster, ViewingArtworkUtils.resolveCardPoster(item.copy(tmdbPoster = null), preferLocalArtwork = false))
        assertEquals(remotePoster, ViewingArtworkUtils.resolveCardPoster(item.copy(tmdbPoster = null, omdbPoster = null), preferLocalArtwork = false))
        assertEquals("${ViewingArtworkUtils.TMDB_IMAGE_BASE_URL}/w1280/tmdb-backdrop.jpg", ViewingArtworkUtils.resolveCardPoster(item.copy(tmdbPoster = null, omdbPoster = null, poster = null), preferLocalArtwork = false))
    }

    @Test
    fun resolveCollectionBackdrop_prefersCollectionArtThenItemHeroArt() {
        val item = itemWithAllArtwork()
        val list = ViewingList(
            id = "collection",
            title = "Collection",
            localBackdrop = localBackdrop,
            backdrop = remoteBackdrop,
            artworkItems = listOf(item),
            items = listOf(item)
        )

        assertEquals(localBackdrop, ViewingArtworkUtils.resolveCollectionBackdrop(list, preferLocalArtwork = true))
        assertEquals(remoteBackdrop, ViewingArtworkUtils.resolveCollectionBackdrop(list, preferLocalArtwork = false))
        assertEquals("${ViewingArtworkUtils.TMDB_IMAGE_BASE_URL}/w1280/tmdb-backdrop.jpg", ViewingArtworkUtils.resolveCollectionBackdrop(list.copy(localBackdrop = null, backdrop = null), preferLocalArtwork = false))
    }

    @Test
    fun resolversRejectPlaceholderAndNonImageUrls() {
        val generatedTmdbIdUrl = "https://image.tmdb.org/t/p/w500/12345.jpg"
        val item = ViewingItem(
            id = "invalid",
            title = "Invalid",
            localPoster = "[I WILL PROVIDE POSTER FOLDER PATH LATER]",
            tmdbPoster = generatedTmdbIdUrl,
            omdbPoster = "N/A",
            poster = "https://www.themoviedb.org/movie/12345",
            backdrop = "https://www.themoviedb.org/tv/12345",
            tmdbBackdrop = "null"
        )

        assertNull(ViewingArtworkUtils.resolveHeroBackdrop(item))
        assertNull(ViewingArtworkUtils.resolveCardPoster(item))
        assertFalse(ViewingArtworkUtils.isUsableArtwork(" NULL "))
        assertFalse(ViewingArtworkUtils.isRealImageUrl(generatedTmdbIdUrl))
    }

    private fun itemWithAllArtwork() = ViewingItem(
        id = "item",
        title = "Item",
        localPoster = localPoster,
        tmdbPoster = tmdbPoster,
        omdbPoster = omdbPoster,
        poster = remotePoster,
        localBackdrop = localBackdrop,
        tmdbBackdrop = tmdbBackdrop,
        backdrop = remoteBackdrop
    )
}
