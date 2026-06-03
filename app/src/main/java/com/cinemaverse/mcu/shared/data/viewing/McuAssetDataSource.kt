package com.cinemaverse.mcu.shared.data.viewing

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.json.JSONArray
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
    private const val MCU_TITLES_PATH = "mcu_data/mcu_titles.json"
    private const val POSTERS_PATH = "mcu_data/posters.json"

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
        val posterIndex = loadPosterIndex(assetManager)
        val catalogItems = root.optJSONArray("items").orEmptyObjects()
            .map { it.toViewingItem() }
            .map { it.withLocalPosterFrom(posterIndex) }
        val legacyMcuItems = loadLegacyMcuTitles(assetManager, posterIndex)
        val items = (catalogItems + legacyMcuItems)
            .distinctBy { it.id }
        buildData(items, root.optString("updated").takeUsable())
    }.getOrElse { error ->
        Log.w(TAG, "Unable to load $CATALOG_PATH; falling back to MCU title assets and built-in seed data", error)
        val posterIndex = loadPosterIndex(assetManager)
        val fallbackItems = loadLegacyMcuTitles(assetManager, posterIndex).ifEmpty { ViewingLists.allItems.map { it.withLocalPosterFrom(posterIndex) } }
        buildData(fallbackItems, null)
    }



    private data class PosterIndex(
        val byId: Map<String, String> = emptyMap(),
        val byTitle: Map<String, String> = emptyMap()
    )

    private fun loadPosterIndex(assetManager: AssetManager): PosterIndex = runCatching {
        val root = JSONObject(assetManager.open(POSTERS_PATH).bufferedReader().use { it.readText() })
        PosterIndex(
            byId = root.optJSONObject("byId")?.toStringMap().orEmpty(),
            byTitle = root.optJSONObject("byTitle")?.toStringMap().orEmpty()
        )
    }.getOrElse { error ->
        Log.w(TAG, "Unable to load $POSTERS_PATH", error)
        PosterIndex()
    }

    private fun loadLegacyMcuTitles(assetManager: AssetManager, posterIndex: PosterIndex): List<ViewingItem> = runCatching {
        JSONArray(assetManager.open(MCU_TITLES_PATH).bufferedReader().use { it.readText() })
            .orEmptyObjects()
            .map { json -> json.toLegacyMcuViewingItem(posterIndex) }
    }.getOrElse { error ->
        Log.w(TAG, "Unable to load $MCU_TITLES_PATH", error)
        emptyList()
    }

    private fun ViewingItem.withLocalPosterFrom(posterIndex: PosterIndex): ViewingItem {
        if (!localPoster.isNullOrBlank()) return this
        val local = posterIndex.byId[id]
            ?: tmdbId?.toString()?.let { posterIndex.byId[it] }
            ?: posterIndex.byTitle[title]
            ?: poster?.substringAfterLast('/')?.takeIf { it.endsWith(".jpg", ignoreCase = true) || it.endsWith(".png", ignoreCase = true) }
        return if (local.isNullOrBlank()) this else copy(localPoster = local)
    }

    private fun JSONObject.toLegacyMcuViewingItem(posterIndex: PosterIndex): ViewingItem {
        val id = optString("id")
        val title = optString("title")
        val releaseMillis = optLong("releaseDate").takeIf { it > 0L }
        val releaseDate = releaseMillis?.let { java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneOffset.UTC).toLocalDate().toString() }
        val posterPath = optString("posterPath").takeUsable() ?: posterIndex.byId[id] ?: posterIndex.byTitle[title]
        return ViewingItem(
            id = id,
            title = title,
            universe = "MCU",
            franchise = optString("series").takeUsable(),
            studio = "Marvel Studios",
            type = optString("type").toViewingType(),
            saga = optString("saga").takeUsable(),
            category = "MCU Core",
            releaseDate = releaseDate,
            localPoster = posterPath,
            releaseOrder = optInt("viewingOrder").takeIf { it > 0 },
            chronologicalOrder = optInt("viewingOrder").takeIf { it > 0 },
            metadataSource = MetadataSource.LOCAL
        )
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
            sort: Comparator<ViewingItem> = releaseComparator()
        ) {
            if (filtered.isEmpty()) return
            val listItems = filtered.sortedWith(sort)
            val art = listItems.firstOrNull { !it.poster.isNullOrBlank() || !it.backdrop.isNullOrBlank() }
            lists += ViewingList(
                id = id,
                title = title,
                description = description,
                universe = universe,
                category = category,
                phase = phase,
                saga = saga,
                franchise = franchise,
                poster = art?.poster,
                backdrop = art?.backdrop,
                itemIds = listItems.map { it.id },
                items = listItems
            )
        }

        val marvel = items.filter { it.universe == "MCU" || it.universe == "Marvel" }
        val dc = items.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") }
        list("all-release-order", "Cinemaverse Release Order", "Marvel and DC titles sorted by public release date.", items, category = "Release Order")
        list("mcu-release-order", "MCU Release Order", "Marvel Studios films, series, specials, and One-Shots by release date.", marvel, universe = "MCU", category = "Release Order")
        list("mcu-chronological-order", "MCU Chronological Order", "Official Disney+ timeline-inspired MCU order with Defenders and multiverse labels kept visible.", marvel, universe = "MCU", category = "Chronological Order", sort = chronologicalComparator())
        list("dc-release-order", "DC Release Order", "DCU, DCEU, and Elseworlds titles by public release date.", dc, universe = "DC", category = "Release Order")
        list("dceu-release-order", "DCEU Release Order", "The DCEU theatrical and streaming-era watch order.", items.filter { it.universe == "DCEU" }, universe = "DCEU", category = "Release Order")
        list("dceu-chronological-order", "DCEU Chronological Order", "DCEU stories ordered by in-universe placement where clear.", items.filter { it.universe == "DCEU" }, universe = "DCEU", category = "Chronological Order", sort = chronologicalComparator())
        list("dcu-release-order", "DCU Release Order", "DC Studios Chapter One titles, including clearly marked upcoming entries.", items.filter { it.universe == "DCU" }, universe = "DCU", category = "Release Order")
        list("dc-elseworlds", "DC Elseworlds", "Standalone DC film and TV universes such as Joker and The Batman.", items.filter { it.universe == "Elseworlds" }, universe = "Elseworlds", category = "DC Elseworlds")

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
        list("marvel-specials", "Marvel Specials", "Special Presentations and seasonal MCU entries.", items.filter { it.type == ViewingType.SPECIAL }, universe = "MCU", category = "Specials", sort = releaseComparator())
        list("disney-plus-series", "Disney+ Series", "Marvel Studios streaming series in release order.", items.filter { it.category == "Disney+ Series" }, universe = "MCU", category = "Disney+ Series", sort = releaseComparator())
        list("defenders-saga", "Defenders Saga", "Street-level Marvel Television and Disney+ continuity entries.", items.filter { it.category == "Defenders Saga" }, universe = "MCU", category = "Defenders Saga", sort = releaseComparator())
        return lists.distinctBy { it.id }
    }

    private fun JSONObject.toViewingItem(): ViewingItem {
        val type = optString("type").toViewingType()
        val releaseDate = optString("releaseDate").takeUsable()
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
            poster = optString("poster").takeUsable(),
            tmdbPoster = optString("tmdbPoster").takeUsable(),
            omdbPoster = optString("omdbPoster").takeUsable(),
            backdrop = optString("backdrop").takeUsable(),
            tmdbBackdrop = optString("tmdbBackdrop").takeUsable(),
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

    private fun releaseComparator() = compareBy<ViewingItem> { it.releaseDate ?: "9999-99-99" }.thenBy { it.releaseOrder ?: Int.MAX_VALUE }.thenBy { it.title }
    private fun chronologicalComparator() = compareBy<ViewingItem> { it.chronologicalOrder ?: Int.MAX_VALUE }.thenBy { it.releaseDate ?: "9999-99-99" }.thenBy { it.title }
    private fun phaseComparator() = compareBy<ViewingItem> { it.phaseOrder ?: it.releaseOrder ?: Int.MAX_VALUE }.thenBy { it.releaseDate ?: "9999-99-99" }.thenBy { it.title }
    private fun collectionComparator() = compareBy<ViewingItem> { it.collectionOrder ?: it.releaseOrder ?: Int.MAX_VALUE }.thenBy { it.releaseDate ?: "9999-99-99" }.thenBy { it.title }

    private fun JSONArray?.orEmptyObjects(): List<JSONObject> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }
    private fun JSONArray?.orEmptyStrings(): List<String> = if (this == null) emptyList() else (0 until length()).mapNotNull { optString(it).takeUsable() }
    private fun JSONObject.toStringMap(): Map<String, String> = keys().asSequence().mapNotNull { key -> optString(key).takeUsable()?.let { key to it } }.toMap()

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
    private fun String.extractYoutubeVideoId(): String? {
        val trimmed = trim()
        if (Regex("""^[A-Za-z0-9_-]{11}$""").matches(trimmed)) return trimmed
        return listOf(
            Regex("""(?:youtube\.com|m\.youtube\.com)/watch(?:\?[^#]*)?[?&]v=([A-Za-z0-9_-]{11})"""),
            Regex("""(?:youtu\.be/|youtube\.com/embed/|youtube-nocookie\.com/embed/|youtube\.com/shorts/)([A-Za-z0-9_-]{11})"""),
            Regex("""[?&]v=([A-Za-z0-9_-]{11})""")
        ).firstNotNullOfOrNull { regex -> regex.find(trimmed)?.groupValues?.getOrNull(1) }
    }
}
