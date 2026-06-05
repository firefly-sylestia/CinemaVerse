package com.marvelspectrum.features.local.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.marvelspectrum.features.local.data.database.dao.ArtistDao
import com.marvelspectrum.features.local.data.database.dao.SongArtistDao
import com.marvelspectrum.features.local.data.database.dao.SongDao
import com.marvelspectrum.features.local.data.database.dao.MCUTitleDao
import com.marvelspectrum.features.local.data.database.dao.MCUSeriesDao
import com.marvelspectrum.features.local.data.database.entity.ArtistEntity
import com.marvelspectrum.features.local.data.database.entity.SongArtistEntity
import com.marvelspectrum.features.local.data.database.entity.SongEntity
import com.marvelspectrum.features.local.data.database.entity.MCUTitleEntity
import com.marvelspectrum.features.local.data.database.entity.MCUSeriesEntity

@Database(entities = [SongEntity::class, ArtistEntity::class, SongArtistEntity::class, MCUTitleEntity::class, MCUSeriesEntity::class], version = 7, exportSchema = false)
abstract class RhythmDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun artistDao(): ArtistDao
    abstract fun songArtistDao(): SongArtistDao
    abstract fun mcuTitleDao(): MCUTitleDao
    abstract fun mcuSeriesDao(): MCUSeriesDao

    companion object {
        @Volatile
        private var INSTANCE: RhythmDatabase? = null

        // Migration from version 1 to 2: Add artist and song-artist tables
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create artists table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `artists` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `artworkUri` TEXT, 
                        `numberOfAlbums` INTEGER NOT NULL, 
                        `numberOfTracks` INTEGER NOT NULL, 
                        `groupByAlbumArtist` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)

                // Create song_artists table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `song_artists` (
                        `songId` TEXT NOT NULL, 
                        `artistName` TEXT NOT NULL, 
                        `groupByAlbumArtist` INTEGER NOT NULL, 
                        PRIMARY KEY(`songId`, `artistName`, `groupByAlbumArtist`)
                    )
                """)
            }
        }

        // Migration from version 2 to 3: No schema changes, just version bump
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No schema changes needed, just ensure tables exist
            }
        }

        // Migration from version 3 to 4: Switch from destructive to proper migrations
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No schema changes, just preserve existing data
            }
        }

        // Migration from version 4 to 5: Persist multi-disc ordering metadata.
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE songs ADD COLUMN discNumber INTEGER NOT NULL DEFAULT 1")
            }
        }

        // Migration from version 5 to 6: Persist song-level modified timestamp.
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE songs ADD COLUMN dateModified INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE songs SET dateModified = dateAdded WHERE dateModified = 0")
            }
        }

        // Migration from version 6 to 7: Add MCU titles and series tables for viewing order support.
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create mcu_titles table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `mcu_titles` (
                        `id` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `series` TEXT NOT NULL, 
                        `viewingOrder` INTEGER NOT NULL, 
                        `releaseDate` INTEGER NOT NULL, 
                        `posterPath` TEXT, 
                        `watched` INTEGER NOT NULL DEFAULT 0, 
                        `watchedDate` INTEGER, 
                        `dateAdded` INTEGER NOT NULL, 
                        `dateModified` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)

                // Create mcu_series table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `mcu_series` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `saga` TEXT NOT NULL, 
                        `posterPath` TEXT, 
                        `numberOfTitles` INTEGER NOT NULL, 
                        `watchedCount` INTEGER NOT NULL DEFAULT 0, 
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }

        fun getInstance(context: Context): RhythmDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RhythmDatabase::class.java,
                    "rhythm_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
