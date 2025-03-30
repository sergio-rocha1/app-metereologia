package com.uilover.project2022

/**
 * Modificações de alinhamento e padronização de UI
 * Realizadas por: Cascade - Assistente de IA Codeium
 * Data: 30/03/2025
 * 
 * Melhorias implementadas:
 * - Padronização de dimensões com constantes
 * - Alinhamento consistente de textos e elementos
 * - Espaçamento uniforme entre componentes
 * - Aprimoramento da acessibilidade com descrições de conteúdo
 * - Distribuição equilibrada de elementos na interface
 */

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uilover.project2022.ui.components.ErrorMessage
import com.uilover.project2022.ui.components.LoadingIndicator

// MODIFICAÇÃO: Adicionadas constantes para padronização de dimensões em toda a UI
// Isso garante consistência visual e facilita futuras alterações
private val STANDARD_PADDING = 16.dp
private val STANDARD_PADDING_SMALL = 8.dp
private val STANDARD_PADDING_LARGE = 24.dp
private val ICON_SIZE_SMALL = 30.dp
private val ICON_SIZE_MEDIUM = 40.dp
private val ICON_SIZE_LARGE = 150.dp
private val CORNER_RADIUS = 25.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherScreen()
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }
}

@Preview
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    // Collect state flows from ViewModel
    val currentWeatherState by viewModel.currentWeatherState.collectAsState()
    val hourlyForecastState by viewModel.hourlyForecastState.collectAsState()
    val dailyForecastState by viewModel.dailyForecastState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(android.graphics.Color.parseColor("#59469d")),
                        Color(android.graphics.Color.parseColor("#643d67"))
                    )
                )
            )
    )
    {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Current weather section
                item {
                    when (currentWeatherState) {
                        is CurrentWeatherUiState.Loading -> {
                            LoadingIndicator(modifier = Modifier.padding(top = 100.dp))
                        }
                        is CurrentWeatherUiState.Success -> {
                            val weather = (currentWeatherState as CurrentWeatherUiState.Success).data
                            
                            // MODIFICAÇÃO: Ajustado o alinhamento do texto da condição climática
                            // Substituído fillMaxSize por fillMaxWidth para melhor alinhamento
                            // Adicionado padding horizontal padronizado
                            Text(
                                text = weather.condition,
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp, start = STANDARD_PADDING_LARGE, end = STANDARD_PADDING_LARGE),
                                textAlign = TextAlign.Center
                            )

                            // MODIFICAÇÃO: Melhorado o alinhamento da imagem e adicionada descrição de conteúdo
                            // Substituído tamanho hardcoded por constante ICON_SIZE_LARGE
                            // Adicionado align(Alignment.CenterHorizontally) para centralização explícita
                            Image(
                                painter = painterResource(id = R.drawable.cloudy_sunny),
                                contentDescription = "Weather condition icon",
                                modifier = Modifier
                                    .size(ICON_SIZE_LARGE)
                                    .padding(top = STANDARD_PADDING_SMALL)
                                    .align(Alignment.CenterHorizontally)
                            )

                            //Display date and time
                            // MODIFICAÇÃO: Padronizado o padding horizontal e vertical
                            Text(
                                text = weather.dateTime,
                                fontSize = 19.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = STANDARD_PADDING_SMALL, start = STANDARD_PADDING_LARGE, end = STANDARD_PADDING_LARGE),
                                textAlign = TextAlign.Center
                            )

                            //Display temperature details
                            // MODIFICAÇÃO: Padronizado o padding horizontal e vertical
                            Text(
                                text = "${weather.temperature}°", 
                                fontSize = 63.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = STANDARD_PADDING_SMALL, start = STANDARD_PADDING_LARGE, end = STANDARD_PADDING_LARGE),
                                textAlign = TextAlign.Center
                            )

                            // MODIFICAÇÃO: Padronizado o padding horizontal e vertical
                            Text(
                                text = "H:${weather.highTemp} L:${weather.lowTemp}",
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = STANDARD_PADDING_SMALL, start = STANDARD_PADDING_LARGE, end = STANDARD_PADDING_LARGE),
                                textAlign = TextAlign.Center
                            )

                            //Box containing weather details like rain, wind speed, humidity
                            // MODIFICAÇÃO: Padronizado o padding e o raio de arredondamento
                            // Substituído valores hardcoded por constantes
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = STANDARD_PADDING_LARGE, vertical = STANDARD_PADDING)
                                    .background(
                                        color = colorResource(id = R.color.purple),
                                        shape = RoundedCornerShape(CORNER_RADIUS)
                                    )
                            ) {
                                // MODIFICAÇÃO: Alterado o arranjo horizontal para SpaceEvenly
                                // Isso distribui os elementos uniformemente no espaço disponível
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .padding(horizontal = STANDARD_PADDING_SMALL),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    WeatherDetailItem(
                                        icon = R.drawable.rain,
                                        value = "${weather.rainPercentage}%",
                                        label = "Chuva"
                                    )
                                    WeatherDetailItem(
                                        icon = R.drawable.wind,
                                        value = weather.windSpeed,
                                        label = "Velocidade do Vento"
                                    )
                                    WeatherDetailItem(
                                        icon = R.drawable.humidity,
                                        value = "${weather.humidity}%",
                                        label = "Umidade"
                                    )
                                }
                            }

                            //Displaying "Today" label
                            // MODIFICAÇÃO: Padronizado o padding e adicionado alinhamento de texto explícito
                            Text(
                                text = "Hoje",
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = STANDARD_PADDING_LARGE, vertical = STANDARD_PADDING_SMALL),
                                textAlign = TextAlign.Start
                            )
                        }
                        is CurrentWeatherUiState.Error -> {
                            val errorMessage = (currentWeatherState as CurrentWeatherUiState.Error).message
                            ErrorMessage(message = errorMessage)
                        }
                    }
                }

                //Display future hourly forecast using a LazyRow
                item {
                    when (hourlyForecastState) {
                        is HourlyForecastUiState.Loading -> {
                            LoadingIndicator(
                                modifier = Modifier
                                    .height(150.dp)
                                    .fillMaxWidth()
                            )
                        }
                        is HourlyForecastUiState.Success -> {
                            val hourlyItems = (hourlyForecastState as HourlyForecastUiState.Success).data
                            // MODIFICAÇÃO: Padronizado o espaçamento entre itens e o padding horizontal
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = STANDARD_PADDING_LARGE),
                                horizontalArrangement = Arrangement.spacedBy(STANDARD_PADDING_SMALL)
                            ) {
                                items(hourlyItems) { item ->
                                    FutureModelViewHolder(item)
                                }
                            }
                        }
                        is HourlyForecastUiState.Error -> {
                            val errorMessage = (hourlyForecastState as HourlyForecastUiState.Error).message
                            ErrorMessage(
                                message = errorMessage,
                                modifier = Modifier
                                    .height(150.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }

                //Display "Future" label and next 7 day button
                item {
                    // MODIFICAÇÃO: Padronizado o padding horizontal e vertical
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = STANDARD_PADDING_LARGE, vertical = STANDARD_PADDING),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Futuro",
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Refresh button
                        // MODIFICAÇÃO: Adicionado arredondamento ao botão para combinar com o estilo do app
                        Button(
                            onClick = { viewModel.refreshWeatherData() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.purple)
                            ),
                            shape = RoundedCornerShape(CORNER_RADIUS / 2)
                        ) {
                            Text(
                                text = "Atualizar",
                                color = Color.White
                            )
                        }
                    }
                }

                //Display future daily forecast using a LazyColumn
                item {
                    when (dailyForecastState) {
                        is DailyForecastUiState.Loading -> {
                            LoadingIndicator(
                                modifier = Modifier
                                    .height(300.dp)
                                    .fillMaxWidth()
                            )
                        }
                        is DailyForecastUiState.Success -> {
                            val dailyItems = (dailyForecastState as DailyForecastUiState.Success).data
                            // MODIFICAÇÃO: Adicionado background com transparência e arredondamento
                            // Isso melhora a separação visual da seção de previsão diária
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(horizontal = STANDARD_PADDING_LARGE)
                                    .background(
                                        color = colorResource(id = R.color.purple).copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(CORNER_RADIUS)
                                    )
                                    .padding(vertical = STANDARD_PADDING_SMALL)
                            ) {
                                dailyItems.forEach { item ->
                                    FutureItem(item)
                                }
                            }
                        }
                        is DailyForecastUiState.Error -> {
                            val errorMessage = (dailyForecastState as DailyForecastUiState.Error).message
                            ErrorMessage(
                                message = errorMessage,
                                modifier = Modifier
                                    .height(300.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
                
                // MODIFICAÇÃO: Adicionado espaço no final para melhorar a experiência de rolagem
                // Isso evita que o último item fique cortado na parte inferior da tela
                item {
                    Box(modifier = Modifier.height(STANDARD_PADDING_LARGE))
                }
            }
        }
    }
}

@Composable
fun FutureItem(item: FutureModel) {
    // MODIFICAÇÃO: Padronizado o padding e adicionado arranjo horizontal SpaceBetween
    // Isso distribui os elementos de forma mais equilibrada na linha
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = STANDARD_PADDING_SMALL, horizontal = STANDARD_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.day,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        // MODIFICAÇÃO: Adicionada descrição de conteúdo para acessibilidade
        // Substituído tamanho hardcoded por constante ICON_SIZE_SMALL
        Image(
            painter = painterResource(id = getDrawableResourceId(item.picPath)),
            contentDescription = "Weather icon for ${item.day}",
            modifier = Modifier.size(ICON_SIZE_SMALL),
            contentScale = ContentScale.Fit
        )

        // MODIFICAÇÃO: Adicionado alinhamento de texto centralizado
        Text(
            text = item.status,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .padding(start = STANDARD_PADDING_SMALL),
            textAlign = TextAlign.Center
        )

        // MODIFICAÇÃO: Adicionado alinhamento de texto à direita
        Text(
            text = "${item.highTemp}°/${item.lowTemp}°",
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.End
        )
    }
}

fun getDrawableResourceId(picPath: String): Int {
    return when (picPath) {
        "cloudy" -> R.drawable.cloudy
        "cloudy_sunny" -> R.drawable.cloudy_sunny
        "storm" -> R.drawable.storm
        "sunny" -> R.drawable.sunny
        else -> R.drawable.cloudy
    }
}

@Composable
fun FutureModelViewHolder(model: HourlyModel) {
    // MODIFICAÇÃO: Padronizado o padding e o raio de arredondamento
    Box(
        modifier = Modifier
            .padding(end = STANDARD_PADDING_SMALL)
            .background(
                color = colorResource(id = R.color.purple),
                shape = RoundedCornerShape(CORNER_RADIUS)
            )
    ) {
        // MODIFICAÇÃO: Substituído valor hardcoded por constante STANDARD_PADDING
        Column(
            modifier = Modifier
                .padding(STANDARD_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = model.hour,
                color = Color.White,
                fontSize = 16.sp
            )

            // MODIFICAÇÃO: Adicionada descrição de conteúdo para acessibilidade
            // Substituído tamanho hardcoded por constante ICON_SIZE_MEDIUM
            Image(
                painter = painterResource(id = getDrawableResourceId(model.picPath)),
                contentDescription = "Weather icon for ${model.hour}",
                modifier = Modifier
                    .size(ICON_SIZE_MEDIUM)
                    .padding(vertical = STANDARD_PADDING_SMALL)
            )

            Text(
                text = "${model.temp}°",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun WeatherDetailItem(icon: Int, value: String, label: String) {
    // MODIFICAÇÃO: Adicionado arranjo vertical centralizado e padding horizontal
    // Isso garante que os itens fiquem bem espaçados e alinhados
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = STANDARD_PADDING_SMALL)
    ) {
        // MODIFICAÇÃO: Adicionada descrição de conteúdo para acessibilidade
        // Substituído tamanho hardcoded por constante ICON_SIZE_SMALL
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(ICON_SIZE_SMALL)
        )

        // MODIFICAÇÃO: Padronizado o espaçamento e adicionado alinhamento de texto centralizado
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = STANDARD_PADDING_SMALL / 2),
            textAlign = TextAlign.Center
        )

        // MODIFICAÇÃO: Padronizado o espaçamento e adicionado alinhamento de texto centralizado
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = STANDARD_PADDING_SMALL / 2),
            textAlign = TextAlign.Center
        )
    }
}