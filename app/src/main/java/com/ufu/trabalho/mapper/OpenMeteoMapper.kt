package com.ufu.trabalho.mapper

import UiCurrentWeather
import com.ufu.trabalho.model.FutureModel
import com.ufu.trabalho.model.HourlyModel
import com.ufu.trabalho.model.OpenMeteoResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        // Verifica se os dados horários existem
        val hourly = api.hourly ?: return emptyList()
        // Define o número máximo de horas que deseja exibir
        val count = minOf(10, hourly.time.size)
        return (0 until count).map { i ->
            HourlyModel(
                // Se o formato for ISO (ex.: "2025-03-30T09:00"), extrai a parte de hora
                hour = if (hourly.time[i].length >= 16) hourly.time[i].substring(11, 16) else hourly.time[i],
                temp = hourly.temperature[i].toInt(),
                picPath = codeToPicPath(hourly.weathercode[i])
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
            day // fallback se ocorrer erro
        }
    }
}