package com.ufu.trabalho.ui.components

import UiCurrentWeather
import com.ufu.trabalho.model.FutureModel
import com.ufu.trabalho.model.HourlyModel

sealed class CurrentWeatherUiState {
    object Loading : CurrentWeatherUiState()
    data class Success(val data: UiCurrentWeather) : CurrentWeatherUiState()
    data class Error(val message: String) : CurrentWeatherUiState()
}

sealed class HourlyForecastUiState {
    object Loading : HourlyForecastUiState()
    data class Success(val data: List<HourlyModel>) : HourlyForecastUiState()
    data class Error(val message: String) : HourlyForecastUiState()
}

sealed class DailyForecastUiState {
    object Loading : DailyForecastUiState()
    data class Success(val data: List<FutureModel>) : DailyForecastUiState()
    data class Error(val message: String) : DailyForecastUiState()
}