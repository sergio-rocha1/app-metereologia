package com.ufu.trabalho.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufu.trabalho.mapper.OpenMeteoMapper
import com.ufu.trabalho.repository.WeatherRepository
import com.ufu.trabalho.ui.components.CurrentWeatherUiState
import com.ufu.trabalho.ui.components.DailyForecastUiState
import com.ufu.trabalho.ui.components.HourlyForecastUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    // States para a UI
    private val _currentWeatherState = MutableStateFlow<CurrentWeatherUiState>(CurrentWeatherUiState.Loading)
    val currentWeatherState: StateFlow<CurrentWeatherUiState> = _currentWeatherState

    private val _hourlyForecastState = MutableStateFlow<HourlyForecastUiState>(HourlyForecastUiState.Loading)
    val hourlyForecastState: StateFlow<HourlyForecastUiState> = _hourlyForecastState

    private val _dailyForecastState = MutableStateFlow<DailyForecastUiState>(DailyForecastUiState.Loading)
    val dailyForecastState: StateFlow<DailyForecastUiState> = _dailyForecastState

    fun refreshWeatherData(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                // Loga as coordenadas usadas
                Log.d("WeatherViewModel", "Buscando dados para lat: $lat, lon: $lon")

                // Busca os dados da API Open-Meteo
                val apiResponse = repository.getWeatherData(lat, lon)
                Log.d("WeatherViewModel", "API Response: $apiResponse")

                // Mapeia para o modelo de UI (UiCurrentWeather)
                val currentUi = OpenMeteoMapper.toCurrentWeather(apiResponse)
                Log.d("WeatherViewModel", "Current UI: $currentUi")

                // Busca o nome da localização via geocodificação reversa
                val locationName = repository.getLocationName(lat, lon)
                Log.d("WeatherViewModel", "Nome da localização: $locationName")

                // Atualiza o modelo com o nome da localização
                val updatedUi = currentUi.copy(locationName = locationName)
                Log.d("WeatherViewModel", "Updated UI: $updatedUi")

                // Atualiza o estado de previsão horária (se houver)
                val hourlyList = OpenMeteoMapper.toHourlyList(apiResponse)
                _hourlyForecastState.value = HourlyForecastUiState.Success(hourlyList)

                // Atualiza o estado de previsão diária
                val futureList = OpenMeteoMapper.toFutureList(apiResponse)
                _dailyForecastState.value = DailyForecastUiState.Success(futureList)

                // Atualiza o estado com o modelo atualizado
                _currentWeatherState.value = CurrentWeatherUiState.Success(updatedUi)

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Erro ao buscar dados: ${e.message}", e)
                _currentWeatherState.value = CurrentWeatherUiState.Error("Erro: ${e.message}")
            }
        }
    }

    fun searchAndRefresh(query: String) {
        viewModelScope.launch {
            try {
                // Usa o método de pesquisa para obter o resultado
                val result = repository.searchLocation(query)
                if (result != null) {
                    // Converte as strings de lat e lon para Double e atualiza os dados do clima
                    refreshWeatherData(result.lat.toDouble(), result.lon.toDouble())
                } else {
                    // Se nenhum resultado, você pode atualizar o estado com um erro
                    _currentWeatherState.value = CurrentWeatherUiState.Error("Local não encontrado")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _currentWeatherState.value = CurrentWeatherUiState.Error("Erro na pesquisa: ${e.message}")
            }
        }
    }

}

