package com.example.omoohwo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Poi::class), version = 1, exportSchema = false)
public abstract class PoiDatabase: RoomDatabase() {
    abstract fun poiDao(): PlacesDao

    companion object {
        private var instance: PoiDatabase? = null

        fun getDatabase(ctx: Context) : PoiDatabase {
            var tempInstance = instance
            if(tempInstance == null) {
                tempInstance = Room.databaseBuilder(
                    ctx.applicationContext,
                    PoiDatabase::class.java,
                    "poi_database"
                ).build()
                instance = tempInstance
            }
            return tempInstance
        }
    }
}