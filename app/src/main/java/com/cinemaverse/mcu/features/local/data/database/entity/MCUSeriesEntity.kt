package com.cinemaverse.mcu.features.local.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mcu_series")
data class MCUSeriesEntity(
    @PrimaryKey val id: String,
    val name: String,
    val saga: String, // "The Infinity Saga", "The Multiverse Saga", etc.
    val posterPath: String?, // Series poster
    val numberOfTitles: Int,
    val watchedCount: Int = 0
)
