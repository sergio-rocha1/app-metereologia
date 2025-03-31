package com.ufu.trabalho.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ufu.trabalho.database.DailyForecastEntity
import com.ufu.trabalho.database.HourlyForecastEntity
import com.ufu.trabalho.database.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyForecasts(forecasts: List<DailyForecastEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecasts(forecasts: List<HourlyForecastEntity>)

    @Query("SELECT * FROM weather WHERE locationName = :locationName ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun getWeatherForLocation(locationName: String): WeatherEntity?

    @Query("SELECT * FROM daily_forecast WHERE weatherId = :weatherId ORDER BY date ASC")
    suspend fun getDailyForecastsForWeather(weatherId: Long): List<DailyForecastEntity>

    @Query("SELECT * FROM hourly_forecast WHERE weatherId = :weatherId ORDER BY time ASC")
    suspend fun getHourlyForecastsForWeather(weatherId: Long): List<HourlyForecastEntity>

    @Query("SELECT * FROM weather ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatestWeather(): Flow<WeatherEntity?>

    @Query("DELETE FROM weather WHERE id = :weatherId")
    suspend fun deleteWeather(weatherId: Long)

    @Query("DELETE FROM daily_forecast WHERE weatherId = :weatherId")
    suspend fun deleteDailyForecasts(weatherId: Long)

    @Query("DELETE FROM hourly_forecast WHERE weatherId = :weatherId")
    suspend fun deleteHourlyForecasts(weatherId: Long)

    @Transaction
    suspend fun deleteWeatherWithForecasts(weatherId: Long) {
        deleteDailyForecasts(weatherId)
        deleteHourlyForecasts(weatherId)
        deleteWeather(weatherId)
    }

    @Query("DELETE FROM weather WHERE lastUpdated < :timestamp")
    suspend fun deleteOldWeatherData(timestamp: Long)
}
