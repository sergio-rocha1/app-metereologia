package com.ufu.trabalho.database.repository

import com.ufu.trabalho.database.DailyForecastEntity
import com.ufu.trabalho.database.HourlyForecastEntity
import com.ufu.trabalho.database.LocationEntity
import com.ufu.trabalho.database.WeatherDatabase
import com.ufu.trabalho.database.WeatherEntity
import com.ufu.trabalho.model.CurrentWeather
import com.ufu.trabalho.model.DailyForecast
import com.ufu.trabalho.model.HourlyForecast
import com.ufu.trabalho.model.LocationResult
import com.ufu.trabalho.model.OpenMeteoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Repository class that handles database operations for weather data
 */
class WeatherDatabaseRepository(private val database: WeatherDatabase) {

    // Weather operations
    suspend fun saveWeatherData(response: OpenMeteoResponse, locationName: String): Long {
        return withContext(Dispatchers.IO) {
            // Save the main weather data first
            val weatherId = database.weatherDao().insertWeather(
                WeatherEntity(
                    latitude = response.latitude,
                    longitude = response.longitude,
                    locationName = locationName,
                    currentTemperature = response.currentWeather.temperature,
                    currentWindSpeed = response.currentWeather.windspeed,
                    currentWindDirection = response.currentWeather.winddirection,
                    currentWeatherCode = response.currentWeather.weathercode,
                    currentTime = response.currentWeather.time
                )
            )

            // Save daily forecasts
            val dailyForecasts = response.daily.time.mapIndexed { index, date ->
                DailyForecastEntity(
                    weatherId = weatherId,
                    date = date,
                    temperatureMax = response.daily.temperatureMax[index],
                    temperatureMin = response.daily.temperatureMin[index],
                    weatherCode = response.daily.weathercode[index]
                )
            }
            database.weatherDao().insertDailyForecasts(dailyForecasts)

            // Save hourly forecasts if available
            response.hourly?.let { hourly ->
                val hourlyForecasts = hourly.time.mapIndexed { index, time ->
                    HourlyForecastEntity(
                        weatherId = weatherId,
                        time = time,
                        temperature = hourly.temperature[index],
                        weatherCode = hourly.weathercode[index],
                        relativeHumidity = hourly.relativeHumidity[index],
                        windSpeed = hourly.windSpeed[index],
                        precipitationProbability = hourly.precipitationProbability[index],
                        uvIndex = hourly.uvIndex[index]
                    )
                }
                database.weatherDao().insertHourlyForecasts(hourlyForecasts)
            }

            weatherId
        }
    }

    suspend fun getWeatherForLocation(locationName: String): OpenMeteoResponse? {
        return withContext(Dispatchers.IO) {
            val weatherEntity = database.weatherDao().getWeatherForLocation(locationName) ?: return@withContext null
            val dailyForecasts = database.weatherDao().getDailyForecastsForWeather(weatherEntity.id)
            val hourlyForecasts = database.weatherDao().getHourlyForecastsForWeather(weatherEntity.id)

            // Convert database entities back to model objects
            OpenMeteoResponse(
                latitude = weatherEntity.latitude,
                longitude = weatherEntity.longitude,
                currentWeather = CurrentWeather(
                    temperature = weatherEntity.currentTemperature,
                    windspeed = weatherEntity.currentWindSpeed,
                    winddirection = weatherEntity.currentWindDirection,
                    weathercode = weatherEntity.currentWeatherCode,
                    time = weatherEntity.currentTime
                ),
                daily = createDailyForecastFromEntities(dailyForecasts),
                hourly = createHourlyForecastFromEntities(hourlyForecasts)
            )
        }
    }

    fun getLatestWeather(): Flow<OpenMeteoResponse?> {
        return database.weatherDao().getLatestWeather().map { weatherEntity ->
            weatherEntity?.let {
                val dailyForecasts = database.weatherDao().getDailyForecastsForWeather(it.id)
                val hourlyForecasts = database.weatherDao().getHourlyForecastsForWeather(it.id)

                OpenMeteoResponse(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    currentWeather = CurrentWeather(
                        temperature = it.currentTemperature,
                        windspeed = it.currentWindSpeed,
                        winddirection = it.currentWindDirection,
                        weathercode = it.currentWeatherCode,
                        time = it.currentTime
                    ),
                    daily = createDailyForecastFromEntities(dailyForecasts),
                    hourly = createHourlyForecastFromEntities(hourlyForecasts)
                )
            }
        }
    }

    suspend fun deleteOldWeatherData() {
        withContext(Dispatchers.IO) {
            // Delete weather data older than 24 hours
            val cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
            database.weatherDao().deleteOldWeatherData(cutoffTime)
        }
    }

    // Location operations
    suspend fun saveLocation(location: LocationResult): Long {
        return withContext(Dispatchers.IO) {
            val locationEntity = LocationEntity(
                displayName = location.displayName,
                latitude = location.lat,
                longitude = location.lon
            )
            database.locationDao().insertLocation(locationEntity)
        }
    }

    fun getAllLocations(): Flow<List<LocationEntity>> {
        return database.locationDao().getAllLocations()
    }

    suspend fun searchLocations(query: String): List<LocationResult> {
        return withContext(Dispatchers.IO) {
            database.locationDao().searchLocations(query).map {
                LocationResult(
                    displayName = it.displayName,
                    lat = it.latitude,
                    lon = it.longitude
                )
            }
        }
    }

    // Helper methods
    private fun createDailyForecastFromEntities(entities: List<DailyForecastEntity>): DailyForecast {
        val times = mutableListOf<String>()
        val maxTemps = mutableListOf<Double>()
        val minTemps = mutableListOf<Double>()
        val weatherCodes = mutableListOf<Int>()

        entities.forEach { entity ->
            times.add(entity.date)
            maxTemps.add(entity.temperatureMax)
            minTemps.add(entity.temperatureMin)
            weatherCodes.add(entity.weatherCode)
        }

        return DailyForecast(
            time = times,
            temperatureMax = maxTemps,
            temperatureMin = minTemps,
            weathercode = weatherCodes
        )
    }

    private fun createHourlyForecastFromEntities(entities: List<HourlyForecastEntity>): HourlyForecast? {
        if (entities.isEmpty()) return null

        val times = mutableListOf<String>()
        val temps = mutableListOf<Double>()
        val weatherCodes = mutableListOf<Int>()
        val humidities = mutableListOf<Int>()
        val windSpeeds = mutableListOf<Double>()
        val precipitationProbabilities = mutableListOf<Int>()
        val uvIndices = mutableListOf<Double>()

        entities.forEachIndexed { index, entity ->
            times.add(entity.time)
            temps.add(entity.temperature)
            weatherCodes.add(entity.weatherCode)
            if (index == 0) {
                // Para a hora atual, utilize os valores reais
                humidities.add(entity.relativeHumidity)
                windSpeeds.add(entity.windSpeed)
                precipitationProbabilities.add(entity.precipitationProbability)
                uvIndices.add(entity.uvIndex)
            } else {
                // Para as demais horas, atribua valores padrão (ou os que você desejar)
                humidities.add(0)
                windSpeeds.add(0.0)
                precipitationProbabilities.add(0)
                uvIndices.add(0.0)
            }
        }

        return HourlyForecast(
            time = times,
            temperature = temps,
            weathercode = weatherCodes.toList(),
            relativeHumidity = humidities.toList(),
            windSpeed = windSpeeds.toList(),
            precipitationProbability = precipitationProbabilities.toList(),
            uvIndex = uvIndices.toList()
        )
    }
}
