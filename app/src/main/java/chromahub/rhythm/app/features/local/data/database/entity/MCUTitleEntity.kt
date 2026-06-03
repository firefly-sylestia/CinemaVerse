package chromahub.rhythm.app.features.local.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mcu_titles")
data class MCUTitleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // "movie" or "series"
    val series: String, // e.g., "Avengers", "Spider-Man", "Guardians"
    val viewingOrder: Int, // Official MCU viewing order number
    val releaseDate: Long, // Release date in milliseconds
    val posterPath: String?, // Path to poster image
    val watched: Boolean = false,
    val watchedDate: Long? = null, // Date when user watched it
    val dateAdded: Long,
    val dateModified: Long
)
