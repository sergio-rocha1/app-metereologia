package com.ufu.trabalho.model

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("current_weather")
    val currentWeather: CurrentWeather,
    val daily: DailyForecast,
    @SerializedName("daily_units")
    val dailyUnits: DailyUnits? = null,
    val hourly: HourlyForecast? = null  // novo campo para dados horários
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Double,
    val weathercode: Int,
    val time: String
)

data class DailyForecast(
    val time: List<String>,
    @SerializedName("temperature_2m_max")
    val temperatureMax: List<Double>,
    @SerializedName("temperature_2m_min")
    val temperatureMin: List<Double>,
    val weathercode: List<Int>
)

data class DailyUnits(
    val time: String,
    @SerializedName("temperature_2m_max")
    val temperatureMax: String,
    @SerializedName("temperature_2m_min")
    val temperatureMin: String,
    val weathercode: String
)

data class HourlyForecast(
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature: List<Double>,
    val weathercode: List<Int>
)