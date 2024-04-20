package com.example.omoohwo

import androidx.room.*;

@Dao
interface PlacesDao {
    @Query("SELECT * FROM places WHERE id = :id")
    fun getPoiById(id: Long): Poi?

    @Query("SELECT * FROM places")
    fun getAllPoi(): List<Poi>

    @Insert
    fun insert(venue: Poi): Long

    @Update
    fun update(venue: Poi): Int

    @Delete
    fun delete(venue: Poi): Int
}