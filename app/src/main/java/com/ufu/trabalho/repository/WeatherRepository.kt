package com.ufu.trabalho.repository

import android.content.Context
import com.google.gson.Gson
import com.ufu.trabalho.database.LocationEntity
import com.ufu.trabalho.database.WeatherDatabase
import com.ufu.trabalho.model.LocationResult
import com.ufu.trabalho.model.OpenMeteoResponse
import com.ufu.trabalho.model.ReverseGeocodeResponse
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class WeatherRepository(private val context: Context) {

    // Configuração do Ktor Client com suporte a JSON (para Open-Meteo)
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    // Função para buscar os dados da API Open-Meteo
    suspend fun getWeatherData(lat: Double, lon: Double): OpenMeteoResponse {
        val response: HttpResponse = client.get("https://api.open-meteo.com/v1/forecast") {
            url {
                parameters.append("latitude", lat.toString())
                parameters.append("longitude", lon.toString())
                // Dados diários (por exemplo, máxima/mínima e probabilidade de chuva máxima)
                parameters.append("daily", "temperature_2m_max,temperature_2m_min,weathercode,precipitation_probability_max")
                // Dados horários: além de temperatura e código, adicionamos umidade, velocidade do vento, probabilidade de chuva e UV
                parameters.append("hourly", "temperature_2m,weathercode,relativehumidity_2m,wind_speed_10m,precipitation_probability,uv_index")
                parameters.append("current_weather", "true")
                parameters.append("forecast_days", "7")
                parameters.append("timezone", "America/Sao_Paulo")
            }
        }
        val jsonString = response.bodyAsText()
        return Gson().fromJson(jsonString, OpenMeteoResponse::class.java)
    }

    // Função para buscar o nome da localização usando a API Nominatim (OpenStreetMap)
    suspend fun getLocationName(lat: Double, lon: Double): String {
        val response: HttpResponse = client.get("https://nominatim.openstreetmap.org/reverse") {
            url {
                parameters.append("lat", lat.toString())
                parameters.append("lon", lon.toString())
                parameters.append("format", "json")
            }
            header("User-Agent", "AppDeMeteorologia/1.0 (seuemail@dominio.com)")
        }
        val jsonString = response.bodyAsText()
        val geocodeResponse = Gson().fromJson(jsonString, ReverseGeocodeResponse::class.java)
        return geocodeResponse.address?.city
            ?: geocodeResponse.address?.town
            ?: geocodeResponse.address?.village
            ?: "Local desconhecido"
    }

    // Função para buscar localização pela query (cidade, estado, país)
    suspend fun searchLocation(query: String): LocationResult? {
        val client = HttpClient() // ou reutilize seu client se preferir
        val url = "https://nominatim.openstreetmap.org/search"
        val response: HttpResponse = client.get(url) {
            url {
                parameters.append("q", query)
                parameters.append("format", "json")
            }
            header("User-Agent", "AppDeMeteorologia/1.0 (seuemail@dominio.com)")
        }
        val jsonString = response.bodyAsText()
        val results = Gson().fromJson(jsonString, Array<LocationResult>::class.java)
        return results.firstOrNull()
    }

    // *** Métodos para acesso ao banco via Room ***

    suspend fun insertLocation(location: LocationEntity): Long {
        return WeatherDatabase.getDatabase(context).locationDao().insertLocation(location)
    }

    fun getAllLocations(): Flow<List<LocationEntity>> {
        return WeatherDatabase.getDatabase(context).locationDao().getAllLocations()
    }

    suspend fun deleteLocation(locationId: Long) {
        WeatherDatabase.getDatabase(context).locationDao().deleteLocation(locationId)
    }
}