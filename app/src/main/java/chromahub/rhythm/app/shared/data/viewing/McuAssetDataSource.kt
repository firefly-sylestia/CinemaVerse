package chromahub.rhythm.app.shared.data.viewing

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.time.Instant
import java.time.ZoneOffset

private const val TAG = "McuAssetDataSource"
private const val DATA_DIR = "mcu_data"
private const val POSTER_DIR = "mcu_posters"
private const val TITLES_JSON = "$DATA_DIR/mcu_titles.json"
private const val POSTERS_JSON = "$DATA_DIR/posters.json"
private const val ASSET_POSTER_PREFIX = "file:///android_asset/$POSTER_DIR/"

data class ViewingCatalog(
    val allItems: List<ViewingItem>,
    val allLists: List<ViewingList>,
    val featuredList: ViewingList,
    val featuredItem: ViewingItem
) {
    fun findItem(id: String): ViewingItem? = allItems.firstOrNull { item ->
        item.id == id || item.imdbId == id || item.tmdbId?.toString() == id
    }

    fun findList(id: String): ViewingList? = allLists.firstOrNull { it.id == id }

    fun search(query: String): Pair<List<ViewingItem>, List<ViewingList>> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return allItems.take(8) to allLists.take(6)
        return allItems.filter { item ->
            listOfNotNull(item.title, item.year, item.phase, item.saga, item.franchise, item.director)
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

class McuAssetDataSource(private val context: Context) {
    fun loadCatalog(): ViewingCatalog {
        val assets = context.assets
        val records = readTitleRecords(assets)
        val posterIndex = buildPosterIndex(assets, records, readPosterMap(assets))
        val enrichedItems = ViewingLists.releaseItems.map { curated ->
            val record = records.firstOrNull { it.matches(curated) }
            curated.mergeWith(record, posterIndex[canonical(curated.title)] ?: posterIndex[canonical(curated.id)])
        }
        val byId = enrichedItems.associateBy { it.id }
        val lists = ViewingLists.allLists.map { list ->
            val mergedItems = list.items.map { item -> byId[item.id] ?: item }
            list.copy(
                items = mergedItems,
                localPoster = mergedItems.firstNotNullOfOrNull { it.localPoster },
                localBackdrop = mergedItems.firstNotNullOfOrNull { it.localBackdrop }
            )
        }
        val featured = byId[ViewingLists.featuredItem.id] ?: enrichedItems.firstOrNull() ?: ViewingLists.featuredItem
        return ViewingCatalog(
            allItems = enrichedItems,
            allLists = lists,
            featuredList = lists.firstOrNull() ?: ViewingLists.featuredList,
            featuredItem = featured
        )
    }

    private fun readTitleRecords(assets: AssetManager): List<McuTitleRecord> = runCatching {
        assets.open(TITLES_JSON).bufferedReader().use { reader ->
            JsonParser.parseReader(reader).asJsonArray.mapNotNull { element ->
                val obj = element.asJsonObject
                McuTitleRecord(
                    id = obj.stringOrNull("id"),
                    title = obj.stringOrNull("title").orEmpty(),
                    type = obj.stringOrNull("type"),
                    series = obj.stringOrNull("series"),
                    saga = obj.stringOrNull("saga"),
                    viewingOrder = obj.intOrNull("viewingOrder"),
                    releaseDate = obj.longOrNull("releaseDate")?.toIsoDate(),
                    posterPath = obj.stringOrNull("posterPath")?.takeIf { poster -> assets.exists("$POSTER_DIR/$poster") }
                ).takeIf { it.title.isNotBlank() }
            }
        }
    }.onFailure { Log.w(TAG, "Unable to load bundled MCU title JSON", it) }.getOrDefault(emptyList())

    private fun readPosterMap(assets: AssetManager): Map<String, String> = runCatching {
        val root = assets.open(POSTERS_JSON).bufferedReader().use { JsonParser.parseReader(it).asJsonObject }
        buildMap {
            root.getAsJsonObject("byId")?.entrySet()?.forEach { (id, value) ->
                val poster = value.asString
                if (assets.exists("$POSTER_DIR/$poster")) put(id, poster) else Log.w(TAG, "Poster listed for id $id is missing: $poster")
            }
            root.getAsJsonObject("byTitle")?.entrySet()?.forEach { (title, value) ->
                val poster = value.asString
                if (assets.exists("$POSTER_DIR/$poster")) put(canonical(title), poster) else Log.w(TAG, "Poster listed for $title is missing: $poster")
            }
        }
    }.onFailure { Log.w(TAG, "Unable to load bundled MCU poster JSON", it) }.getOrDefault(emptyMap())

    private fun buildPosterIndex(
        assets: AssetManager,
        records: List<McuTitleRecord>,
        posterMap: Map<String, String>
    ): Map<String, String> = buildMap {
        assets.list(POSTER_DIR).orEmpty().forEach { fileName ->
            put(canonical(fileName.titleFromPosterName()), "$ASSET_POSTER_PREFIX$fileName")
        }
        records.forEach { record ->
            val poster = record.posterPath ?: record.id?.let { posterMap[it] } ?: posterMap[canonical(record.title)]
            if (!poster.isNullOrBlank()) put(canonical(record.title), "$ASSET_POSTER_PREFIX$poster")
        }
        posterMap.forEach { (key, poster) ->
            if (poster.contains('.')) put(key, "$ASSET_POSTER_PREFIX$poster")
        }
    }

    private fun ViewingItem.mergeWith(record: McuTitleRecord?, localPosterPath: String?): ViewingItem = copy(
        type = record?.type?.toViewingType() ?: type,
        franchise = franchise ?: record?.series ?: "Marvel Cinematic Universe",
        saga = saga ?: record?.saga?.removePrefix("The "),
        order = order ?: record?.viewingOrder,
        chronologicalOrder = chronologicalOrder ?: record?.viewingOrder,
        releaseDate = releaseDate ?: record?.releaseDate,
        year = year ?: record?.releaseDate?.take(4),
        localPoster = localPosterPath ?: localPoster,
        poster = localPosterPath ?: poster
    )
}

private data class McuTitleRecord(
    val id: String?,
    val title: String,
    val type: String?,
    val series: String?,
    val saga: String?,
    val viewingOrder: Int?,
    val releaseDate: String?,
    val posterPath: String?
) {
    fun matches(item: ViewingItem): Boolean = canonical(title) == canonical(item.title)
}

private fun JsonObject.stringOrNull(name: String): String? = get(name)?.takeUnless { it.isJsonNull }?.asString
private fun JsonObject.intOrNull(name: String): Int? = get(name)?.takeUnless { it.isJsonNull }?.asInt
private fun JsonObject.longOrNull(name: String): Long? = get(name)?.takeUnless { it.isJsonNull }?.asLong

private fun Long.toIsoDate(): String = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().toString()

private fun String?.toViewingType(): ViewingType = when (this?.lowercase()) {
    "series", "tv", "show" -> ViewingType.SERIES
    "episode" -> ViewingType.EPISODE
    "special" -> ViewingType.SPECIAL
    "short", "one-shot", "oneshot" -> ViewingType.SHORT
    else -> ViewingType.MOVIE
}

private fun String.titleFromPosterName(): String = substringBeforeLast('.')
    .replace(Regex("^\\d+-"), "")
    .replace('-', ' ')
    .replace(Regex("\\s+"), " ")
    .trim()

private fun canonical(value: String): String = value
    .lowercase()
    .replace("&", "and")
    .replace(Regex("[^a-z0-9]"), "")

private fun AssetManager.exists(path: String): Boolean = runCatching { open(path).close() }.isSuccess
