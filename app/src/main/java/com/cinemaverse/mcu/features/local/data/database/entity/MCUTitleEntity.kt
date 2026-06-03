package com.cinemaverse.mcu.features.local.data.database.entity

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
    val dateModified: Long,
    val favorite: Boolean = false,
    val watchlisted: Boolean = false,
    val notes: String? = null,
    val userRating: Int = 0,
    val openedCount: Int = 0,
    val lastOpenedDate: Long? = null,
    val saga: String? = null,
    val phase: String? = null,
    val genres: String? = null,
    val runtime: String? = null,
    val trailerUrl: String? = null,
    val youtubeVideoId: String? = null,
    val overview: String? = null
)
