package com.ufu.trabalho.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ufu.trabalho.database.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    @Query("SELECT * FROM location ORDER BY lastAccessed DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM location WHERE isFavorite = 1 ORDER BY displayName ASC")
    fun getFavoriteLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM location WHERE displayName LIKE '%' || :query || '%' ORDER BY lastAccessed DESC LIMIT 10")
    suspend fun searchLocations(query: String): List<LocationEntity>

    @Query("UPDATE location SET isFavorite = :isFavorite WHERE id = :locationId")
    suspend fun updateFavoriteStatus(locationId: Long, isFavorite: Boolean)

    @Query("UPDATE location SET lastAccessed = :timestamp WHERE id = :locationId")
    suspend fun updateLastAccessed(locationId: Long, timestamp: Long)

    @Query("DELETE FROM location WHERE id = :locationId")
    suspend fun deleteLocation(locationId: Long)

    @Query("SELECT * FROM location WHERE latitude = :latitude AND longitude = :longitude LIMIT 1")
    suspend fun getLocationByCoordinates(latitude: String, longitude: String): LocationEntity?
}
