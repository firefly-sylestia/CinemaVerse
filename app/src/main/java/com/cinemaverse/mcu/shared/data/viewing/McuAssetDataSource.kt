package com.cinemaverse.mcu.shared.data.viewing

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.json.JSONArray
import com.cinemaverse.mcu.shared.util.ViewingArtworkUtils
import org.json.JSONObject

object McuAssetDataSource {
    /**
     * Offline-first catalog seed. Ordering is cross-checked against Marvel's official Disney+
     * MCU timeline article (published June 2, 2026), DC's official catalog pages, and TMDb/OMDb
     * metadata docs. YouTube is intentionally stored only as trailer IDs for IFrame playback,
     * never as poster artwork.
     */
    private const val TAG = "ViewingCatalogDataSource"
    private const val CATALOG_PATH = "viewing/viewing_catalog.json"

    data class ViewingAssetData(
        val allItems: List<ViewingItem>,
        val allLists: List<ViewingList>,
        val featuredItem: ViewingItem,
        val featuredList: ViewingList,
        val lastUpdated: String? = null
    ) {
        fun findItem(id: String?): ViewingItem? = allItems.firstOrNull { it.id == id }
        fun findList(id: String?): ViewingList? = allLists.firstOrNull { it.id == id }

        fun search(query: String): Pair<List<ViewingItem>, List<ViewingList>> {
            val normalized = query.trim().lowercase()
            if (normalized.isBlank()) return allItems.take(18) to allLists.take(12)
            return allItems.filter { item ->
                listOfNotNull(
                    item.title,
                    item.originalTitle,
                    item.year,
                    item.releaseDate,
                    item.phase,
                    item.saga,
                    item.franchise,
                    item.universe,
                    item.category,
                    item.studio,
                    item.director,
                    item.writer,
                    item.imdbId
                ).any { it.lowercase().contains(normalized) } ||
                    item.genres.any { it.lowercase().contains(normalized) } ||
                    item.actors.any { it.lowercase().contains(normalized) } ||
                    item.cast.any { it.name.lowercase().contains(normalized) || it.character.orEmpty().lowercase().contains(normalized) }
            } to allLists.filter { list ->
                listOfNotNull(list.title, list.description, list.universe, list.category, list.phase, list.saga, list.franchise)
                    .any { it.lowercase().contains(normalized) } ||
                    list.items.any { it.title.lowercase().contains(normalized) }
            }
        }
    }

    fun load(context: Context): ViewingAssetData = load(context.assets)

    fun load(assetManager: AssetManager): ViewingAssetData = runCatching {
        val root = JSONObject(assetManager.open(CATALOG_PATH).bufferedReader().use { it.readText() })
        val localAssets = assetManager.list("mcu_posters").orEmpty().toSet()
        val items = root.optJSONArray("items").orEmptyObjects()
            .map { it.toViewingItem(localAssets) }
            .distinctBy { it.id }
        buildData(items, root.optString("updated").takeUsable())
    }.getOrElse { error ->
        Log.w(TAG, "Unable to load $CATALOG_PATH; falling back to built-in seed data", error)
        buildData(ViewingLists.allItems, null)
    }

    private fun buildData(items: List<ViewingItem>, updated: String?): ViewingAssetData {
        val sortedItems = items.sortedWith(compareBy<ViewingItem> { it.universe ?: "" }.thenBy { it.releaseDate ?: "9999-99-99" }.thenBy { it.releaseOrder ?: Int.MAX_VALUE })
        val lists = buildViewingLists(sortedItems)
        val featuredList = lists.firstOrNull { it.id == "mcu-release-order" } ?: lists.first()
        val featuredItem = sortedItems.firstOrNull { it.id == "mcu-iron-man" }
            ?: featuredList.items.firstOrNull()
            ?: sortedItems.first()
        return ViewingAssetData(sortedItems, lists, featuredItem, featuredList, updated)
    }

    private fun buildViewingLists(items: List<ViewingItem>): List<ViewingList> {
        val lists = mutableListOf<ViewingList>()
        fun list(
            id: String,
            title: String,
            description: String,
            filtered: List<ViewingItem>,
            universe: String? = null,
            category: String? = null,
            phase: String? = null,
            saga: String? = null,
            franchise: String? = null,
            sort: Comparator<ViewingItem> = releaseComparator(),
            importance: ViewingListImportance = ViewingListImportance.SECONDARY
        ) {
            if (filtered.isEmpty()) return
            val listItems = filtered.sortedWith(sort)
            val art = listItems.firstOrNull { ViewingArtworkUtils.resolvePoster(it) != null || ViewingArtworkUtils.resolveBackdrop(it) != null }
            lists += ViewingList(
                id = id,
                title = title,
                description = description,
                universe = universe,
                category = category,
                phase = phase,
                saga = saga,
                franchise = franchise,
                poster = art?.let { ViewingArtworkUtils.resolvePoster(it, preferLocalArtwork = false) },
                localPoster = art?.localPoster,
                backdrop = art?.let { ViewingArtworkUtils.resolveBackdrop(it, preferLocalArtwork = false) },
                localBackdrop = art?.localBackdrop,
                itemIds = listItems.map { it.id },
                items = listItems,
                importance = importance
            )
        }

        val marvel = items.filter { it.universe == "MCU" || it.universe == "Marvel" }
        val dc = items.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") }
        list("all-release-order", "Cinemaverse Release Order", "Marvel and DC titles sorted by public release date.", items, category = "Release Order", importance = ViewingListImportance.PRIMARY)
        list("mcu-release-order", "MCU Release Order", "Marvel Studios films, series, specials, and One-Shots by release date.", marvel, universe = "MCU", category = "Release Order", importance = ViewingListImportance.PRIMARY)
        list("mcu-chronological-order", "MCU Chronological Order", "Official Disney+ timeline-inspired MCU order with Defenders and multiverse labels kept visible.", marvel, universe = "MCU", category = "Chronological Order", sort = chronologicalComparator(), importance = ViewingListImportance.PRIMARY)
        list("dc-release-order", "DC Release Order", "DCU, DCEU, and Elseworlds titles by public release date.", dc, universe = "DC", category = "Release Order", importance = ViewingListImportance.PRIMARY)
        list("dceu-release-order", "DCEU Release Order", "The DCEU theatrical and streaming-era watch order.", items.filter { it.universe == "DCEU" }, universe = "DCEU", category = "Release Order", importance = ViewingListImportance.PRIMARY)
        list("dceu-chronological-order", "DCEU Chronological Order", "DCEU stories ordered by in-universe placement where clear.", items.filter { it.universe == "DCEU" }, universe = "DCEU", category = "Chronological Order", sort = chronologicalComparator(), importance = ViewingListImportance.PRIMARY)
        list("dcu-release-order", "DCU Chapter One", "DC Studios Chapter One titles, including clearly marked upcoming entries.", items.filter { it.universe == "DCU" }, universe = "DCU", category = "Chapter One", importance = ViewingListImportance.PRIMARY)
        list("dc-elseworlds", "DC Elseworlds", "Standalone DC film and TV universes such as Joker and The Batman.", items.filter { it.universe == "Elseworlds" }, universe = "Elseworlds", category = "DC Elseworlds", importance = ViewingListImportance.PRIMARY)

        items.groupBy { it.phase }.forEach { (phase, phaseItems) ->
            if (!phase.isNullOrBlank()) list(phase.slug(), phase, "${phaseItems.size} titles in $phase.", phaseItems, phase = phase, category = "Phases / Chapters", sort = phaseComparator())
        }
        items.groupBy { it.saga }.forEach { (saga, sagaItems) ->
            if (!saga.isNullOrBlank()) list(saga.slug(), saga, "${sagaItems.size} titles across $saga.", sagaItems, saga = saga, category = "Saga Order")
        }
        items.groupBy { it.franchise }.forEach { (franchise, franchiseItems) ->
            if (!franchise.isNullOrBlank() && franchiseItems.size > 1) list(franchise.slug(), franchise, "A focused viewing collection for $franchise.", franchiseItems, franchise = franchise, category = "Collections", sort = collectionComparator())
        }
        list("marvel-one-shots", "Marvel One-Shots", "Short-form MCU connective tissue.", items.filter { it.type == ViewingType.ONE_SHOT }, universe = "MCU", category = "Marvel One-Shots", sort = releaseComparator())
        list("marvel-specials", "Marvel Specials", "Special Presentations and seasonal MCU entries.", items.filter { it.type == ViewingType.SPECIAL }, universe = "MCU", category = "Specials", sort = releaseComparator(), importance = ViewingListImportance.PRIMARY)
        list("disney-plus-series", "Disney+ Series", "Marvel Studios streaming series in release order.", items.filter { it.category == "Disney+ Series" }, universe = "MCU", category = "Disney+ Series", sort = releaseComparator())
        list("defenders-saga", "Defenders Saga", "Street-level Marvel Television and Disney+ continuity entries.", items.filter { it.category == "Defenders Saga" }, universe = "MCU", category = "Defenders Saga", sort = releaseComparator(), importance = ViewingListImportance.PRIMARY)
        return lists.distinctBy { it.id }
    }

    private fun JSONObject.toViewingItem(localAssets: Set<String>): ViewingItem {
        val type = optString("type").toViewingType()
        val releaseDate = optString("releaseDate").takeUsable()
        val explicitLocalPoster = optString("localPoster").takeUsable()?.let(ViewingArtworkUtils::localPoster)
        val explicitLocalBackdrop = optString("localBackdrop").takeUsable()?.let(ViewingArtworkUtils::localPoster)
        val resolvedLocalPoster = explicitLocalPoster ?: resolveLocalArtwork(localAssets, optString("id"), optString("title"))
        val youtubeId = optString("youtubeVideoId").takeUsable() ?: optString("trailerUrl").takeUsable()?.extractYoutubeVideoId()
        return ViewingItem(
            id = optString("id"),
            title = optString("title"),
            originalTitle = optString("originalTitle").takeUsable(),
            universe = optString("universe").takeUsable(),
            franchise = optString("franchise").takeUsable(),
            studio = optString("studio").takeUsable(),
            type = type,
            phase = optString("phase").takeUsable(),
            saga = optString("saga").takeUsable(),
            category = optString("category").takeUsable(),
            releaseDate = releaseDate,
            year = optString("year").takeUsable() ?: releaseDate?.take(4),
            runtime = optString("runtime").takeUsable(),
            genres = optJSONArray("genres").orEmptyStrings(),
            language = optString("language").takeUsable(),
            country = optString("country").takeUsable(),
            imdbId = optString("imdbId").takeUsable(),
            tmdbId = optInt("tmdbId").takeIf { it > 0 },
            imdbRating = optString("imdbRating").takeUsable(),
            tmdbRating = optDouble("tmdbRating").takeIf { !it.isNaN() && it > 0.0 },
            director = optString("director").takeUsable(),
            writer = optString("writer").takeUsable(),
            actors = optJSONArray("actors").orEmptyStrings(),
            description = optString("description").takeUsable(),
            overview = optString("overview").takeUsable(),
            plot = optString("plot").takeUsable(),
            poster = optString("poster").takeUsable()?.let { ViewingArtworkUtils.tmdbPoster(it) ?: it },
            tmdbPoster = ViewingArtworkUtils.tmdbPoster(optString("tmdbPoster").takeUsable() ?: optString("poster").takeUsable()?.takeIf { it.contains("themoviedb.org/t/p/") }),
            omdbPoster = optString("omdbPoster").takeUsable(),
            localPoster = resolvedLocalPoster,
            backdrop = optString("backdrop").takeUsable()?.let { ViewingArtworkUtils.tmdbBackdrop(it) ?: it },
            tmdbBackdrop = ViewingArtworkUtils.tmdbBackdrop(optString("tmdbBackdrop").takeUsable() ?: optString("backdrop").takeUsable()?.takeIf { it.contains("themoviedb.org/t/p/") }),
            localBackdrop = explicitLocalBackdrop,
            trailerUrl = optString("trailerUrl").takeUsable() ?: youtubeId?.let { "https://www.youtube.com/watch?v=$it" },
            youtubeVideoId = youtubeId,
            trailerSource = youtubeId?.let { TrailerSource.YOUTUBE },
            releaseOrder = optInt("releaseOrder").takeIf { it > 0 },
            chronologicalOrder = optInt("chronologicalOrder").takeIf { it >= 0 },
            phaseOrder = optInt("phaseOrder").takeIf { it > 0 },
            collectionOrder = optInt("collectionOrder").takeIf { it > 0 },
            metadataSource = MetadataSource.LOCAL,
            lastUpdated = optString("lastUpdated").takeUsable(),
            status = optString("status").toViewingStatus()
        )
    }

    private fun resolveLocalArtwork(localAssets: Set<String>, id: String, title: String): String? {
        if (localAssets.isEmpty()) return null
        val idSlug = id.slug().removePrefix("mcu-")
        val titleSlug = title.slug()
            .removePrefix("marvel-one-shot-")
            .replace("-and-", "-")
            .replace("the-", "")
        fun assetCore(name: String) = name.substringBeforeLast('.').replace(Regex("^\\d+-"), "").slug()
        val exact = localAssets.firstOrNull { assetCore(it) == idSlug || assetCore(it) == titleSlug }
        if (exact != null) return ViewingArtworkUtils.localPoster(exact)
        val reliable = localAssets.firstOrNull { asset ->
            val core = assetCore(asset)
            (titleSlug.length >= 8 && (core.contains(titleSlug) || titleSlug.contains(core))) ||
                (idSlug.length >= 8 && (core.contains(idSlug) || idSlug.contains(core)))
        }
        return reliable?.let(ViewingArtworkUtils::localPoster)
    }

    private fun releaseComparator() = compareBy<ViewingItem> { it.releaseDate ?: "9999-99-99" }.thenBy { it.releaseOrder ?: Int.MAX_VALUE }.thenBy { it.title }
    private fun chronologicalComparator() = compareBy<ViewingItem> { it.chronologicalOrder ?: Int.MAX_VALUE }.thenBy { it.releaseDate ?: "9999-99-99" }.thenBy { it.title }
    private fun phaseComparator() = compareBy<ViewingItem> { it.phaseOrder ?: it.releaseOrder ?: Int.MAX_VALUE }.thenBy { it.releaseDate ?: "9999-99-99" }.thenBy { it.title }
    private fun collectionComparator() = compareBy<ViewingItem> { it.collectionOrder ?: it.releaseOrder ?: Int.MAX_VALUE }.thenBy { it.releaseDate ?: "9999-99-99" }.thenBy { it.title }

    private fun JSONArray?.orEmptyObjects(): List<JSONObject> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }
    private fun JSONArray?.orEmptyStrings(): List<String> = if (this == null) emptyList() else (0 until length()).mapNotNull { optString(it).takeUsable() }

    private fun String?.takeUsable(): String? = this?.takeIf { it.isNotBlank() && it != "N/A" }
    private fun String.toViewingType(): ViewingType = when (uppercase()) {
        "SERIES", "TV", "SHOW" -> ViewingType.SERIES
        "EPISODE" -> ViewingType.EPISODE
        "SPECIAL" -> ViewingType.SPECIAL
        "SHORT" -> ViewingType.SHORT
        "ONE_SHOT", "ONESHOT", "ONE-SHOT" -> ViewingType.ONE_SHOT
        else -> ViewingType.MOVIE
    }
    private fun String.toViewingStatus(): ViewingStatus = when (uppercase()) {
        "UPCOMING" -> ViewingStatus.UPCOMING
        "ANNOUNCED" -> ViewingStatus.ANNOUNCED
        else -> ViewingStatus.RELEASED
    }
    private fun String.slug(): String = lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    private fun String.extractYoutubeVideoId(): String? = Regex("(?:v=|youtu\\.be/|embed/)([A-Za-z0-9_-]{11})").find(this)?.groupValues?.getOrNull(1)
}
