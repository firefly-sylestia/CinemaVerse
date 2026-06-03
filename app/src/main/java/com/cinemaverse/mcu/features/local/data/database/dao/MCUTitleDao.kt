package com.cinemaverse.mcu.features.local.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import com.cinemaverse.mcu.features.local.data.database.entity.MCUTitleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MCUTitleDao {
    @Query("SELECT * FROM mcu_titles ORDER BY viewingOrder ASC")
    fun getAllTitles(): Flow<List<MCUTitleEntity>>

    @Query("SELECT * FROM mcu_titles WHERE series = :series ORDER BY viewingOrder ASC")
    fun getTitlesBySeries(series: String): Flow<List<MCUTitleEntity>>

    @Query("SELECT * FROM mcu_titles WHERE watched = 1 ORDER BY watchedDate DESC")
    fun getWatchedTitles(): Flow<List<MCUTitleEntity>>

    @Query("SELECT * FROM mcu_titles WHERE id = :id")
    fun getTitleById(id: String): Flow<MCUTitleEntity>

    @Query("SELECT DISTINCT series FROM mcu_titles ORDER BY series ASC")
    fun getAllSeries(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM mcu_titles WHERE watched = 1")
    fun getWatchedCount(): Flow<Int>

    @Insert
    suspend fun insertTitle(title: MCUTitleEntity)

    @Insert
    suspend fun insertTitles(titles: List<MCUTitleEntity>)

    @Update
    suspend fun updateTitle(title: MCUTitleEntity)

    @Delete
    suspend fun deleteTitle(title: MCUTitleEntity)

    @Query("DELETE FROM mcu_titles")
    suspend fun deleteAllTitles()

    @Query("UPDATE mcu_titles SET watched = 1, watchedDate = :watchedDate WHERE id = :id")
    suspend fun markAsWatched(id: String, watchedDate: Long)

    @Query("UPDATE mcu_titles SET watched = 0, watchedDate = NULL WHERE id = :id")
    suspend fun markAsUnwatched(id: String)
}
