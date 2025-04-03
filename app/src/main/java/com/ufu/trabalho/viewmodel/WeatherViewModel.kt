package com.ufu.trabalho.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ufu.trabalho.database.LocationEntity
import com.ufu.trabalho.database.WeatherDatabase
import com.ufu.trabalho.mapper.OpenMeteoMapper
import com.ufu.trabalho.mapper.WeatherExtras
import com.ufu.trabalho.repository.WeatherRepository
import com.ufu.trabalho.ui.components.CurrentWeatherUiState
import com.ufu.trabalho.ui.components.DailyForecastUiState
import com.ufu.trabalho.ui.components.HourlyForecastUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application)

    // States para a UI
    private val _currentWeatherState = MutableStateFlow<CurrentWeatherUiState>(CurrentWeatherUiState.Loading)
    val currentWeatherState: StateFlow<CurrentWeatherUiState> = _currentWeatherState

    private val _hourlyForecastState = MutableStateFlow<HourlyForecastUiState>(HourlyForecastUiState.Loading)
    val hourlyForecastState: StateFlow<HourlyForecastUiState> = _hourlyForecastState

    private val _dailyForecastState = MutableStateFlow<DailyForecastUiState>(DailyForecastUiState.Loading)
    val dailyForecastState: StateFlow<DailyForecastUiState> = _dailyForecastState

    // Novo estado para os dados extras
    private val _weatherExtrasState = MutableStateFlow<WeatherExtras?>(null)
    val weatherExtrasState: StateFlow<WeatherExtras?> = _weatherExtrasState

    // Expondo as localizações salvas
    val savedLocations: Flow<List<LocationEntity>> = repository.getAllLocations()

    fun refreshWeatherData(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                Log.d("WeatherViewModel", "Buscando dados para lat: $lat, lon: $lon")
                val apiResponse = repository.getWeatherData(lat, lon)
                Log.d("WeatherViewModel", "API Response: $apiResponse")

                // Atualiza os dados extras
                val extras = OpenMeteoMapper.toWeatherExtras(apiResponse)
                _weatherExtrasState.value = extras

                val currentUi = OpenMeteoMapper.toCurrentWeather(apiResponse)
                Log.d("WeatherViewModel", "Current UI: $currentUi")
                val locationName = repository.getLocationName(lat, lon)
                Log.d("WeatherViewModel", "Nome da localização: $locationName")
                val updatedUi = currentUi.copy(locationName = locationName)
                Log.d("WeatherViewModel", "Updated UI: $updatedUi")

                _hourlyForecastState.value = HourlyForecastUiState.Success(OpenMeteoMapper.toHourlyList(apiResponse))
                _dailyForecastState.value = DailyForecastUiState.Success(OpenMeteoMapper.toFutureList(apiResponse))
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
                val result = repository.searchLocation(query)
                if (result != null) {
                    val lat = result.lat.toDouble()
                    val lon = result.lon.toDouble()
                    refreshWeatherData(lat, lon)
                    val locationName = result.displayName ?: query
                    val newLocation = LocationEntity(
                        displayName = locationName,
                        latitude = lat.toString(),
                        longitude = lon.toString(),
                        lastAccessed = System.currentTimeMillis()
                    )
                    repository.insertLocation(newLocation)
                } else {
                    _currentWeatherState.value = CurrentWeatherUiState.Error("Local não encontrado")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _currentWeatherState.value = CurrentWeatherUiState.Error("Erro na pesquisa: ${e.message}")
            }
        }
    }

    fun deleteLocation(locationId: Long) {
        viewModelScope.launch {
            repository.deleteLocation(locationId)
        }
    }

    fun updateLocationAccessed(locationId: Long) {
        viewModelScope.launch {
            WeatherDatabase.getDatabase(getApplication()).locationDao()
                .updateLastAccessed(locationId, System.currentTimeMillis())
        }
    }
}

