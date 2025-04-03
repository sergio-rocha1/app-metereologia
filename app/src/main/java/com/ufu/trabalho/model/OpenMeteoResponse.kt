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
    val hourly: HourlyForecast? = null
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
    val weathercode: List<Int>,
    @SerializedName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int>? = null  // opcional, se disponível
)

data class DailyUnits(
    val time: String,
    @SerializedName("temperature_2m_max")
    val temperatureMax: String,
    @SerializedName("temperature_2m_min")
    val temperatureMin: String,
    val weathercode: String,
    @SerializedName("precipitation_probability_max")
    val precipitationProbabilityMax: String? = null
)

data class HourlyForecast(
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature: List<Double>,
    val weathercode: List<Int>,
    @SerializedName("relativehumidity_2m")
    val relativeHumidity: List<Int>,
    @SerializedName("wind_speed_10m")
    val windSpeed: List<Double>,
    @SerializedName("precipitation_probability")
    val precipitationProbability: List<Int>,
    @SerializedName("uv_index")
    val uvIndex: List<Double>
)