package com.ufu.trabalho.mapper

import UiCurrentWeather
import com.ufu.trabalho.model.FutureModel
import com.ufu.trabalho.model.HourlyModel
import com.ufu.trabalho.model.OpenMeteoResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class WeatherExtras(
    val precipitationProbability: Int,
    val windSpeed: Double,
    val humidity: Int,
    val uvIndex: Double
)

object OpenMeteoMapper {

    fun toCurrentWeather(api: OpenMeteoResponse): UiCurrentWeather {
        val current = api.currentWeather
        val daily = api.daily
        // Verifica se há dados na lista; se não, usa 0
        val high = daily.temperatureMax.firstOrNull()?.toInt() ?: 0
        val low = daily.temperatureMin.firstOrNull()?.toInt() ?: 0

        // Mapeia o weathercode para uma string
        val conditionString = when (current.weathercode) {
            0 -> "Céu limpo"
            in 1..3 -> "Parcialmente Nublado"
            in 80..82 -> "Chuva"
            else -> "Desconhecido"
        }

        // Neste exemplo, os dados extras serão tratados separadamente (via toWeatherExtras)
        return UiCurrentWeather(
            temperature = current.temperature.toInt(),
            condition = conditionString,
            highTemp = high,
            lowTemp = low,
            rainPercentage = 22, // Valor padrão; substitua se tiver valor real
            windSpeed = "${current.windspeed.toInt()} Km/h",
            humidity = 18, // Valor padrão; os dados extras virão de outra função
            dateTime = current.time,
            locationName = ""
        )
    }

    fun toHourlyList(api: OpenMeteoResponse): List<HourlyModel> {
        val hourly = api.hourly ?: return emptyList()
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
        val now = Date()
        val futureIndices = hourly.time.indices.filter { i ->
            try {
                val forecastTime = parser.parse(hourly.time[i])
                forecastTime != null && forecastTime.after(now)
            } catch (e: Exception) {
                false
            }
        }
        val count = minOf(10, futureIndices.size)
        return futureIndices.take(count).map { i ->
            HourlyModel(
                hour = if (hourly.time[i].length >= 16) hourly.time[i].substring(11, 16) else hourly.time[i],
                temp = hourly.temperature[i].toInt(),
                picPath = codeToPicPath(hourly.weathercode[i])
            )
        }
    }

    fun toFutureList(api: OpenMeteoResponse): List<FutureModel> {
        val daily = api.daily
        return daily.time.indices.map { i ->
            val day = daily.time[i]
            val high = daily.temperatureMax[i].toInt()
            val low = daily.temperatureMin[i].toInt()
            val code = daily.weathercode[i]
            FutureModel(
                day = dayToWeekday(day),
                picPath = codeToPicPath(code),
                status = codeToStatus(code),
                highTemp = high,
                lowTemp = low
            )
        }
    }

    /**
     * Mapeia os dados extras dos parâmetros horários.
     * Neste exemplo, usamos os valores do primeiro índice da lista.
     */
    fun toWeatherExtras(api: OpenMeteoResponse): WeatherExtras {
        val hourly = api.hourly
        return if (hourly != null && hourly.time.isNotEmpty()) {
            WeatherExtras(
                precipitationProbability = hourly.precipitationProbability.getOrNull(0) ?: 0,
                windSpeed = hourly.windSpeed.getOrNull(0) ?: 0.0,
                humidity = hourly.relativeHumidity.getOrNull(0) ?: 0,
                uvIndex = hourly.uvIndex.getOrNull(0) ?: 0.0
            )
        } else {
            WeatherExtras(0, 0.0, 0, 0.0)
        }
    }

    // Funções auxiliares
    private fun codeToPicPath(code: Int): String {
        return when (code) {
            0 -> "sunny"
            in 1..3 -> "cloudy_sunny"
            in 4..48 -> "storm"
            else -> "cloudy"
        }
    }

    private fun codeToStatus(code: Int): String {
        return when (code) {
            0 -> "Ensolarado"
            in 1..3 -> "Parcialmente Nublado"
            in 80..82 -> "Chuva"
            else -> "Desconhecido"
        }
    }

    private fun dayToWeekday(day: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(day)
            val calendar = Calendar.getInstance().apply { time = date }
            when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Seg"
                Calendar.TUESDAY -> "Ter"
                Calendar.WEDNESDAY -> "Qua"
                Calendar.THURSDAY -> "Qui"
                Calendar.FRIDAY -> "Sex"
                Calendar.SATURDAY -> "Sáb"
                Calendar.SUNDAY -> "Dom"
                else -> day
            }
        } catch (e: Exception) {
            day
        }
    }
}