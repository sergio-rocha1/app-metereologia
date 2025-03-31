// Dados do clima atual que a UI espera
data class UiCurrentWeather(
    val temperature: Int,
    val condition: String,
    val highTemp: Int,
    val lowTemp: Int,
    val rainPercentage: Int,
    val windSpeed: String,
    val humidity: Int,
    val dateTime: String,
    val locationName: String // novo campo para o nome da cidade
)

// Dados de previsão horária (a UI exibe numa LazyRow)
data class HourlyModel(
    val hour: String,    // ex: "09:00"
    val temp: Int,       // ex: 28
    val picPath: String  // ex: "cloudy_sunny"
)

// Dados de previsão diária (a UI exibe numa LazyColumn)
data class FutureModel(
    val day: String,     // ex: "Sab"
    val picPath: String, // ex: "storm"
    val status: String,  // ex: "Tempestade"
    val highTemp: Int,   // ex: 24
    val lowTemp: Int     // ex: 12
)
