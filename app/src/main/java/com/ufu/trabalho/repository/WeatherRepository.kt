package com.ufu.trabalho.repository

import com.google.gson.Gson
import com.ufu.trabalho.model.LocationResult
import com.ufu.trabalho.model.OpenMeteoResponse
import com.ufu.trabalho.model.ReverseGeocodeResponse
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class WeatherRepository {

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
                parameters.append("daily", "temperature_2m_max,temperature_2m_min,weathercode")
                parameters.append("current_weather", "true")
                parameters.append("hourly", "temperature_2m,weathercode") // adiciona dados horários reais
                parameters.append("forecast_days", "7") // Adicionado para garantir 7 dias de previsão
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
            // É importante definir um User-Agent válido para a API do Nominatim
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
            // Nominatim exige um User-Agent
            header("User-Agent", "AppDeMeteorologia/1.0 (seuemail@dominio.com)")
        }
        val jsonString = response.bodyAsText()
        // A resposta é uma lista de resultados, pegamos o primeiro, se houver
        val results = Gson().fromJson(jsonString, Array<LocationResult>::class.java)
        return results.firstOrNull()
    }
}