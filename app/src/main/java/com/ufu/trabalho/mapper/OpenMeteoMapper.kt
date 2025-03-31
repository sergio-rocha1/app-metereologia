package com.ufu.trabalho.mapper

import UiCurrentWeather
import com.ufu.trabalho.model.FutureModel
import com.ufu.trabalho.model.HourlyModel
import com.ufu.trabalho.model.OpenMeteoResponse

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

        val humidity = 18

        return UiCurrentWeather(
            temperature = current.temperature.toInt(),
            condition = conditionString,
            highTemp = high,
            lowTemp = low,
            rainPercentage = 22,
            windSpeed = "${current.windspeed.toInt()} Km/h",
            humidity = humidity,
            dateTime = current.time,
            locationName = ""
        )
    }

    fun toHourlyList(api: OpenMeteoResponse): List<HourlyModel> {
        // Exemplo fixo para previsão horária
        val hours = listOf("09:00", "10:00", "11:00", "12:00", "13:00")
        val temps = listOf(28, 27, 26, 25, 24)
        val weatherCodes = listOf(1, 1, 1, 2, 2)

        return hours.indices.map { i ->
            HourlyModel(
                hour = hours[i],
                temp = temps[i],
                picPath = codeToPicPath(weatherCodes[i])
            )
        }
    }

    fun toFutureList(api: OpenMeteoResponse): List<FutureModel> {
        val daily = api.daily
        return daily.time.indices.map { i ->
            val day = daily.time[i]  // ex: "2025-03-25"
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
        // Exemplo simplificado: converter "2025-03-25" em "Seg"
        return "Seg"
    }
}