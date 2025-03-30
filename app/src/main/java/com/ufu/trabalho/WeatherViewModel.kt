package com.uilover.project2022

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the UI state for the weather app using coroutines
 */
class WeatherViewModel : ViewModel() {
    
    private val repository = WeatherRepository()
    
    // State flows for different parts of the UI
    private val _currentWeatherState = MutableStateFlow<CurrentWeatherUiState>(CurrentWeatherUiState.Loading)
    val currentWeatherState: StateFlow<CurrentWeatherUiState> = _currentWeatherState.asStateFlow()
    
    private val _hourlyForecastState = MutableStateFlow<HourlyForecastUiState>(HourlyForecastUiState.Loading)
    val hourlyForecastState: StateFlow<HourlyForecastUiState> = _hourlyForecastState.asStateFlow()
    
    private val _dailyForecastState = MutableStateFlow<DailyForecastUiState>(DailyForecastUiState.Loading)
    val dailyForecastState: StateFlow<DailyForecastUiState> = _dailyForecastState.asStateFlow()
    
    init {
        loadAllWeatherData()
    }
    
    /**
     * Loads all weather data using coroutines
     */
    fun loadAllWeatherData() {
        loadCurrentWeather()
        loadHourlyForecast()
        loadDailyForecast()
    }
    
    /**
     * Loads current weather data
     */
    private fun loadCurrentWeather() {
        viewModelScope.launch {
            _currentWeatherState.value = CurrentWeatherUiState.Loading
            try {
                repository.getCurrentWeather().collect { weather ->
                    _currentWeatherState.value = CurrentWeatherUiState.Success(weather)
                }
            } catch (e: Exception) {
                _currentWeatherState.value = CurrentWeatherUiState.Error("Failed to load current weather: ${e.message}")
            }
        }
    }
    
    /**
     * Loads hourly forecast data
     */
    private fun loadHourlyForecast() {
        viewModelScope.launch {
            _hourlyForecastState.value = HourlyForecastUiState.Loading
            try {
                repository.getHourlyForecast().collect { forecast ->
                    _hourlyForecastState.value = HourlyForecastUiState.Success(forecast)
                }
            } catch (e: Exception) {
                _hourlyForecastState.value = HourlyForecastUiState.Error("Failed to load hourly forecast: ${e.message}")
            }
        }
    }
    
    /**
     * Loads daily forecast data
     */
    private fun loadDailyForecast() {
        viewModelScope.launch {
            _dailyForecastState.value = DailyForecastUiState.Loading
            try {
                repository.getDailyForecast().collect { forecast ->
                    _dailyForecastState.value = DailyForecastUiState.Success(forecast)
                }
            } catch (e: Exception) {
                _dailyForecastState.value = DailyForecastUiState.Error("Failed to load daily forecast: ${e.message}")
            }
        }
    }
    
    /**
     * Refreshes all weather data
     */
    fun refreshWeatherData() {
        loadAllWeatherData()
    }
}

/**
 * Sealed class representing UI states for current weather
 */
sealed class CurrentWeatherUiState {
    data object Loading : CurrentWeatherUiState()
    data class Success(val data: CurrentWeather) : CurrentWeatherUiState()
    data class Error(val message: String) : CurrentWeatherUiState()
}

/**
 * Sealed class representing UI states for hourly forecast
 */
sealed class HourlyForecastUiState {
    data object Loading : HourlyForecastUiState()
    data class Success(val data: List<HourlyModel>) : HourlyForecastUiState()
    data class Error(val message: String) : HourlyForecastUiState()
}

/**
 * Sealed class representing UI states for daily forecast
 */
sealed class DailyForecastUiState {
    data object Loading : DailyForecastUiState()
    data class Success(val data: List<FutureModel>) : DailyForecastUiState()
    data class Error(val message: String) : DailyForecastUiState()
}
