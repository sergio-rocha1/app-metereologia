package com.ufu.trabalho.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val currentTemperature: Double,
    val currentWindSpeed: Double,
    val currentWindDirection: Double,
    val currentWeatherCode: Int,
    val currentTime: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_forecast")
data class DailyForecastEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weatherId: Long, // Foreign key to WeatherEntity
    val date: String,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val weatherCode: Int
)

@Entity(tableName = "hourly_forecast")
data class HourlyForecastEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weatherId: Long, // foreign key para WeatherEntity
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val relativeHumidity: Int,
    val windSpeed: Double,
    val precipitationProbability: Int,
    val uvIndex: Double
)

@Entity(tableName = "location")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayName: String,
    val latitude: String,
    val longitude: String,
    val isFavorite: Boolean = false,
    val lastAccessed: Long = System.currentTimeMillis()
)
