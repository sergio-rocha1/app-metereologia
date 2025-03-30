package com.uilover.project2022

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository class that simulates fetching weather data from a remote source
 * using Kotlin coroutines and Flow
 */
class WeatherRepository {
    
    /**
     * Simulates fetching current weather data
     * @return Flow emitting the current weather data
     */
    fun getCurrentWeather(): Flow<CurrentWeather> = flow {
        // Simulate network delay
        delay(1500)
        emit(
            CurrentWeather(
                temperature = 25,
                condition = "Parcialmente Nublado",
                highTemp = 27,
                lowTemp = 18,
                rainPercentage = 22,
                windSpeed = "12 Km/h",
                humidity = 18,
                dateTime = "Dom Março 25 | 13:07"
            )
        )
    }.flowOn(Dispatchers.IO)
    
    /**
     * Simulates fetching hourly forecast data
     * @return Flow emitting a list of hourly forecasts
     */
    fun getHourlyForecast(): Flow<List<HourlyModel>> = flow {
        // Simulate network delay
        delay(1000)
        emit(
            listOf(
                HourlyModel("09:00", 28, "cloudy"),
                HourlyModel("10:00", 27, "cloudy"),
                HourlyModel("11:00", 26, "cloudy"),
                HourlyModel("12:00", 25, "cloudy"),
                HourlyModel("13:00", 24, "cloudy"),
                HourlyModel("14:00", 23, "cloudy"),
                HourlyModel("15:00", 22, "cloudy")
            )
        )
    }.flowOn(Dispatchers.IO)
    
    /**
     * Simulates fetching daily forecast data
     * @return Flow emitting a list of daily forecasts
     */
    fun getDailyForecast(): Flow<List<FutureModel>> = flow {
        // Simulate network delay
        delay(2000)
        emit(
            listOf(
                FutureModel("Sab", "storm", "Tempestade", 24, 12),
                FutureModel("Dom", "cloudy", "Nublado", 25, 16),
                FutureModel("Seg", "sunny", "Ensolarado", 29, 18),
                FutureModel("Ter", "cloudy_sunny", "Parcialmente Ensolarado", 27, 17),
                FutureModel("Qua", "storm", "Tempestade", 23, 15),
                FutureModel("Qui", "sunny", "Ensolarado", 30, 19),
                FutureModel("Sex", "cloudy", "Nublado", 26, 16)
            )
        )
    }.flowOn(Dispatchers.IO)
}

/**
 * Data class representing current weather conditions
 */
data class CurrentWeather(
    val temperature: Int,
    val condition: String,
    val highTemp: Int,
    val lowTemp: Int,
    val rainPercentage: Int,
    val windSpeed: String,
    val humidity: Int,
    val dateTime: String
)
