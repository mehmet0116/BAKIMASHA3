package com.assanhanil.techassist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.assanhanil.techassist.data.local.dao.BearingDao
import com.assanhanil.techassist.data.local.dao.ReportDao
import com.assanhanil.techassist.data.local.entity.BearingEntity
import com.assanhanil.techassist.data.local.entity.RecipeEntity
import com.assanhanil.techassist.data.local.entity.ReportEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database for ASSANHANİL TECH-ASSIST.
 * Offline-first architecture with autosave functionality.
 */
@Database(
    entities = [
        BearingEntity::class,
        ReportEntity::class,
        RecipeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TechAssistDatabase : RoomDatabase() {

    abstract fun bearingDao(): BearingDao
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var INSTANCE: TechAssistDatabase? = null

        fun getDatabase(context: Context): TechAssistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TechAssistDatabase::class.java,
                    "techassist_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback to populate the database with initial bearing data.
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                // Use a limited scope that completes when the database operation is done
                kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                    try {
                        populateBearingData(database.bearingDao())
                    } catch (_: Exception) {
                        // Silently handle any errors during initial population
                    }
                }
            }
        }

        /**
         * Pre-populate the database with common bearing specifications.
         */
        private suspend fun populateBearingData(bearingDao: BearingDao) {
            // Deep Groove Ball Bearings - Most common types
            val bearings = listOf(
                // 6200 Series
                BearingEntity(isoCode = "6200-ZZ", innerDiameter = 10.0, outerDiameter = 30.0, width = 9.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6201-ZZ", innerDiameter = 12.0, outerDiameter = 32.0, width = 10.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6202-ZZ", innerDiameter = 15.0, outerDiameter = 35.0, width = 11.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6203-ZZ", innerDiameter = 17.0, outerDiameter = 40.0, width = 12.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6204-ZZ", innerDiameter = 20.0, outerDiameter = 47.0, width = 14.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6205-ZZ", innerDiameter = 25.0, outerDiameter = 52.0, width = 15.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6206-ZZ", innerDiameter = 30.0, outerDiameter = 62.0, width = 16.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6207-ZZ", innerDiameter = 35.0, outerDiameter = 72.0, width = 17.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6208-ZZ", innerDiameter = 40.0, outerDiameter = 80.0, width = 18.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                
                // 6300 Series (Heavy Duty)
                BearingEntity(isoCode = "6300-ZZ", innerDiameter = 10.0, outerDiameter = 35.0, width = 11.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6301-ZZ", innerDiameter = 12.0, outerDiameter = 37.0, width = 12.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6302-ZZ", innerDiameter = 15.0, outerDiameter = 42.0, width = 13.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6303-ZZ", innerDiameter = 17.0, outerDiameter = 47.0, width = 14.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6304-ZZ", innerDiameter = 20.0, outerDiameter = 52.0, width = 15.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                BearingEntity(isoCode = "6305-ZZ", innerDiameter = 25.0, outerDiameter = 62.0, width = 17.0, type = "Deep Groove Ball Bearing", sealType = "ZZ"),
                
                // 2RS (Rubber Seal) variants
                BearingEntity(isoCode = "6204-2RS", innerDiameter = 20.0, outerDiameter = 47.0, width = 14.0, type = "Deep Groove Ball Bearing", sealType = "2RS"),
                BearingEntity(isoCode = "6205-2RS", innerDiameter = 25.0, outerDiameter = 52.0, width = 15.0, type = "Deep Groove Ball Bearing", sealType = "2RS"),
                BearingEntity(isoCode = "6206-2RS", innerDiameter = 30.0, outerDiameter = 62.0, width = 16.0, type = "Deep Groove Ball Bearing", sealType = "2RS"),
                
                // Tapered Roller Bearings
                BearingEntity(isoCode = "30204", innerDiameter = 20.0, outerDiameter = 47.0, width = 15.25, type = "Tapered Roller Bearing", sealType = ""),
                BearingEntity(isoCode = "30205", innerDiameter = 25.0, outerDiameter = 52.0, width = 16.25, type = "Tapered Roller Bearing", sealType = ""),
                BearingEntity(isoCode = "30206", innerDiameter = 30.0, outerDiameter = 62.0, width = 17.25, type = "Tapered Roller Bearing", sealType = ""),
                
                // Needle Roller Bearings
                BearingEntity(isoCode = "HK2020", innerDiameter = 20.0, outerDiameter = 26.0, width = 20.0, type = "Needle Roller Bearing", sealType = ""),
                BearingEntity(isoCode = "HK2520", innerDiameter = 25.0, outerDiameter = 32.0, width = 20.0, type = "Needle Roller Bearing", sealType = "")
            )
            
            bearingDao.insertBearings(bearings)
        }
    }
}
