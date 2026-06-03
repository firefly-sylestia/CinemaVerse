package chromahub.rhythm.app.shared.data.viewing

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import chromahub.rhythm.app.shared.util.ViewingArtworkUtils
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId

object McuAssetDataSource {
    private const val TAG = "McuAssetDataSource"
    private const val TITLE_DATA_PATH = "mcu_data/mcu_titles.json"
    private const val POSTER_DATA_PATH = "mcu_data/posters.json"
    private const val POSTER_ASSET_DIR = "mcu_posters"
    private const val POSTER_ASSET_URL_PREFIX = "file:///android_asset/$POSTER_ASSET_DIR/"

    data class ViewingAssetData(
        val allItems: List<ViewingItem>,
        val allLists: List<ViewingList>,
        val featuredItem: ViewingItem,
        val featuredList: ViewingList
    ) {
        fun search(query: String): Pair<List<ViewingItem>, List<ViewingList>> {
            val normalized = query.trim().lowercase()
            if (normalized.isBlank()) return allItems.take(8) to allLists.take(6)
            return allItems.filter { item ->
                listOfNotNull(item.title, item.year, item.releaseDate, item.phase, item.saga, item.franchise, item.director)
                    .any { it.lowercase().contains(normalized) } ||
                    item.genres.any { it.lowercase().contains(normalized) } ||
                    item.actors.any { it.lowercase().contains(normalized) }
            } to allLists.filter { list ->
                listOfNotNull(list.title, list.description, list.phase, list.saga, list.franchise)
                    .any { it.lowercase().contains(normalized) } ||
                    list.items.any { it.title.lowercase().contains(normalized) }
            }
        }
    }

    private data class JsonTitle(
        val id: String,
        val title: String,
        val type: ViewingType,
        val series: String?,
        val saga: String?,
        val viewingOrder: Int?,
        val releaseDate: String?,
        val year: String?,
        val posterPath: String?
    )

    fun load(context: Context): ViewingAssetData = load(context.assets)

    fun load(assetManager: AssetManager): ViewingAssetData {
        val curatedItems = ViewingLists.allItems
        val curatedLists = ViewingLists.allLists
        val availablePosterFiles = assetManager.list(POSTER_ASSET_DIR).orEmpty().toSet()
        val jsonTitles = readJsonTitles(assetManager)
        val posterMap = readPosterMap(assetManager)

        val jsonByTitle = jsonTitles.associateBy { normalize(it.title) }
        val jsonByOrder = jsonTitles.mapNotNull { title -> title.viewingOrder?.let { it to title } }.toMap()
        val availableBySlug = availablePosterFiles.associateBy { normalizePosterFilename(it) }

        val enrichedCurated = curatedItems.map { curated ->
            val json = jsonByTitle[normalize(curated.title)]
                ?: jsonByOrder[curated.chronologicalOrder]
                ?: jsonByOrder[curated.releaseOrder]
            val posterPath = json?.posterPath
                ?: posterMap.byTitle[normalize(curated.title)]
                ?: availableBySlug[normalize(curated.title)]
            curated.mergeJson(json, assetPosterUrl(assetManager, availablePosterFiles, posterPath))
        }

        val curatedByTitle = enrichedCurated.associateBy { normalize(it.title) }
        val additions = jsonTitles
            .filterNot { curatedByTitle.containsKey(normalize(it.title)) }
            .map { json -> json.toViewingItem(assetPosterUrl(assetManager, availablePosterFiles, json.posterPath ?: posterMap.byId[json.id])) }

        val allItems = (enrichedCurated + additions)
            .distinctBy { normalize(it.title) }
            .sortedBy { it.chronologicalOrder ?: it.order ?: it.releaseOrder ?: Int.MAX_VALUE }
        val byId = allItems.associateBy { it.id }
        val byTitle = allItems.associateBy { normalize(it.title) }
        val allLists = curatedLists.map { list ->
            val items = list.items.map { original -> byId[original.id] ?: byTitle[normalize(original.title)] ?: original }
            val listPoster = list.localPoster ?: items.firstNotNullOfOrNull { it.localPoster }
            list.copy(items = items, localPoster = listPoster, localBackdrop = list.localBackdrop ?: listPoster)
        }
        val featuredList = allLists.firstOrNull() ?: ViewingList("mcu-timeline", "MCU Timeline", items = allItems)
        val featuredItem = allItems.firstOrNull { it.id == ViewingLists.featuredItem.id }
            ?: allItems.firstOrNull { it.title == ViewingLists.featuredItem.title }
            ?: allItems.first()

        return ViewingAssetData(allItems, allLists, featuredItem, featuredList)
    }

    private fun ViewingItem.mergeJson(json: JsonTitle?, localPosterUrl: String?): ViewingItem = copy(
        type = json?.type ?: type,
        saga = saga ?: json?.saga,
        franchise = franchise ?: json?.series ?: "Marvel Cinematic Universe",
        order = order ?: json?.viewingOrder,
        chronologicalOrder = chronologicalOrder ?: json?.viewingOrder,
        releaseDate = releaseDate ?: json?.releaseDate,
        year = year ?: json?.year,
        localPoster = localPosterUrl ?: localPoster,
        localBackdrop = localBackdrop?.takeIf { ViewingArtworkUtils.isUsableArtwork(it) } ?: localPosterUrl
    )

    private fun JsonTitle.toViewingItem(localPosterUrl: String?): ViewingItem = ViewingItem(
        id = "mcu-$id",
        title = title,
        year = year,
        releaseDate = releaseDate,
        type = type,
        saga = saga,
        franchise = series ?: "Marvel Cinematic Universe",
        studio = "Marvel Studios",
        order = viewingOrder,
        chronologicalOrder = viewingOrder,
        phaseOrder = viewingOrder,
        localPoster = localPosterUrl,
        localBackdrop = localPosterUrl,
        trailerUrl = "https://www.youtube.com/results?search_query=${title.replace(" ", "+")}+trailer",
        trailerSource = TrailerSource.MANUAL
    )

    private fun assetPosterUrl(assetManager: AssetManager, availableFiles: Set<String>, posterPath: String?): String? {
        val safePath = posterPath?.takeIf { it.isNotBlank() } ?: return null
        val assetPath = "$POSTER_ASSET_DIR/$safePath"
        return if (safePath in availableFiles || assetManager.exists(assetPath)) {
            "$POSTER_ASSET_URL_PREFIX$safePath"
        } else {
            Log.w(TAG, "Missing bundled poster asset: $assetPath")
            null
        }
    }

    private data class PosterMap(val byId: Map<String, String>, val byTitle: Map<String, String>)

    private fun readPosterMap(assetManager: AssetManager): PosterMap = runCatching {
        val root = JSONObject(assetManager.readText(POSTER_DATA_PATH))
        val byIdJson = root.optJSONObject("byId") ?: JSONObject()
        val byTitleJson = root.optJSONObject("byTitle") ?: JSONObject()
        PosterMap(
            byId = byIdJson.keys().asSequence().associateWith { byIdJson.optString(it) },
            byTitle = byTitleJson.keys().asSequence().associate { normalize(it) to byTitleJson.optString(it) }
        )
    }.getOrElse {
        Log.w(TAG, "Unable to read $POSTER_DATA_PATH", it)
        PosterMap(emptyMap(), emptyMap())
    }

    private fun readJsonTitles(assetManager: AssetManager): List<JsonTitle> = runCatching {
        val array = org.json.JSONArray(assetManager.readText(TITLE_DATA_PATH))
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val releaseDate = item.optLong("releaseDate").takeIf { it > 0L }?.let { epochMillis ->
                Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate().toString()
            }
            JsonTitle(
                id = item.optString("id"),
                title = item.optString("title"),
                type = item.optString("type").toViewingType(),
                series = item.optString("series").takeIf { it.isNotBlank() },
                saga = item.optString("saga").takeIf { it.isNotBlank() },
                viewingOrder = item.optInt("viewingOrder").takeIf { it > 0 },
                releaseDate = releaseDate,
                year = releaseDate?.take(4),
                posterPath = item.optString("posterPath").takeIf { it.isNotBlank() }
            )
        }
    }.getOrElse {
        Log.w(TAG, "Unable to read $TITLE_DATA_PATH", it)
        emptyList()
    }

    private fun String.toViewingType(): ViewingType = when (lowercase()) {
        "series", "tv", "show" -> ViewingType.SERIES
        "episode" -> ViewingType.EPISODE
        "special" -> ViewingType.SPECIAL
        "short" -> ViewingType.SHORT
        else -> ViewingType.MOVIE
    }

    private fun AssetManager.readText(path: String): String = open(path).bufferedReader().use { it.readText() }

    private fun AssetManager.exists(path: String): Boolean = runCatching { open(path).close() }.isSuccess

    private fun normalize(value: String): String = value
        .lowercase()
        .replace(Regex("\b(the|and|in|of|a|an)\b"), "")
        .replace(Regex("[^a-z0-9]+"), "")
        .trim()

    private fun normalizePosterFilename(value: String): String = normalize(
        value.substringBeforeLast('.')
            .replace(Regex("^\\d+-"), "")
    )
}
