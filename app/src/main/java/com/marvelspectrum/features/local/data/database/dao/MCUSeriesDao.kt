package com.marvelspectrum.features.local.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import com.marvelspectrum.features.local.data.database.entity.MCUSeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MCUSeriesDao {
    @Query("SELECT * FROM mcu_series ORDER BY saga ASC, name ASC")
    fun getAllSeries(): Flow<List<MCUSeriesEntity>>

    @Query("SELECT * FROM mcu_series WHERE saga = :saga ORDER BY name ASC")
    fun getSeriesBySaga(saga: String): Flow<List<MCUSeriesEntity>>

    @Query("SELECT * FROM mcu_series WHERE id = :id")
    fun getSeriesById(id: String): Flow<MCUSeriesEntity>

    @Query("SELECT DISTINCT saga FROM mcu_series ORDER BY saga ASC")
    fun getAllSagas(): Flow<List<String>>

    @Insert
    suspend fun insertSeries(series: MCUSeriesEntity)

    @Insert
    suspend fun insertAllSeries(series: List<MCUSeriesEntity>)

    @Update
    suspend fun updateSeries(series: MCUSeriesEntity)

    @Delete
    suspend fun deleteSeries(series: MCUSeriesEntity)

    @Query("DELETE FROM mcu_series")
    suspend fun deleteAllSeries()
}
