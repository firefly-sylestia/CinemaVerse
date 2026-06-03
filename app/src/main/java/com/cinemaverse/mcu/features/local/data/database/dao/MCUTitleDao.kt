package com.cinemaverse.mcu.features.local.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitle(title: MCUTitleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query("UPDATE mcu_titles SET favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("UPDATE mcu_titles SET watchlisted = :watchlisted WHERE id = :id")
    suspend fun setWatchlisted(id: String, watchlisted: Boolean)

    @Query("UPDATE mcu_titles SET userRating = :rating WHERE id = :id")
    suspend fun setRating(id: String, rating: Int)

    @Query("UPDATE mcu_titles SET notes = :notes WHERE id = :id")
    suspend fun setNotes(id: String, notes: String?)

    @Query("UPDATE mcu_titles SET openedCount = openedCount + 1, lastOpenedDate = :openedDate WHERE id = :id")
    suspend fun recordOpened(id: String, openedDate: Long)
}
