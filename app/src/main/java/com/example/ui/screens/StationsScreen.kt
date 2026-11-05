package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioState
import com.example.model.RadioStation
import com.example.ui.RadioViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.AddStationDialog
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan
import com.example.ui.theme.LcdGreen
import com.example.ui.theme.ShareTechMonoFontFamily
import kotlin.math.abs

@Composable
fun StationsScreen(
    stations: List<RadioStation>,
    radioState: RadioState,
    searchQuery: String,
    selectedCategory: String,
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("Tümü", "Favoriler", "Pop", "Haber", "Türkü & Arabesk", "Slow", "Özel Yerel")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = LcdCyan,
                contentColor = Color(0xFF071118),
                shape = CircleShape,
                modifier = Modifier.testTag("add_custom_station_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "İstasyon Ekle")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Özel Frekans Ekle", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("stations_screen"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Arama", tint = Color(0xFF8B949E))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Temizle", tint = Color(0xFF8B949E))
                        }
                    }
                },
                placeholder = { Text("İstasyon, tür veya frekans ara...", color = Color(0xFF6B7280)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("search_station_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LcdCyan,
                    unfocusedBorderColor = Color(0xFF283444),
                    focusedContainerColor = Color(0xFF141A24),
                    unfocusedContainerColor = Color(0xFF141A24)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSel = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF003847) else Color(0xFF1B222C))
                            .border(1.dp, if (isSel) LcdCyan else Color(0xFF2E3846), RoundedCornerShape(8.dp))
                            .clickable { viewModel.setSelectedCategory(cat) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("category_chip_$cat")
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSel) LcdCyan else Color(0xFFD0D7DE),
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            // Stations Count Info
            Text(
                text = "${stations.size} İstasyon Bulundu (İnternetsiz Dinlenebilir)",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF8B949E))
            )

            // Stations List
            if (stations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Aramanıza uygun radyo istasyonu bulunamadı.", color = Color(0xFF8B949E))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kendi yerel frekansınızı eklemek için aşağıdaki butonu kullanabilirsiniz.", color = Color(0xFF566576), fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("stations_lazy_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stations, key = { it.id }) { station ->
                        val isTuned = abs(radioState.currentFrequency - station.frequency) < 0.05f

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFrequency(station.frequency)
                                    if (!radioState.isPlaying) {
                                        viewModel.togglePlayPause()
                                    }
                                    viewModel.setScreen(ScreenTab.TUNER)
                                }
                                .testTag("station_card_${station.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTuned) Color(0xFF132332) else Color(0xFF161C26)
                            ),
                            border = if (isTuned) androidx.compose.foundation.BorderStroke(1.5.dp, LcdCyan) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25303E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Frequency Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isTuned) LcdCyan.copy(alpha = 0.2f) else Color(0xFF222B38))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "%.1f".format(station.frequency),
                                            fontFamily = ShareTechMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = if (isTuned) LcdCyan else Color.White
                                        )
                                        Text(
                                            text = "MHz",
                                            fontFamily = ShareTechMonoFontFamily,
                                            fontSize = 9.sp,
                                            color = if (isTuned) LcdCyan else Color(0xFF8B949E)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Details (Name, Genre, Location)
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = station.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = if (isTuned) LcdAmber else Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        if (station.isCustom) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF00382B))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("ÖZEL", color = LcdGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (isTuned && radioState.isPlaying) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = "Şu Anda Çalıyor",
                                                tint = LcdCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${station.genre} • ${station.location}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9AA4B2))
                                    )
                                    Text(
                                        text = station.currentProgram,
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6E7681)),
                                        maxLines = 1
                                    )
                                }

                                // Actions (Favorite + Delete if custom)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (station.isCustom) {
                                        IconButton(
                                            onClick = { viewModel.removeCustomStation(station.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Özel İstasyonu Sil",
                                                tint = Color(0xFFFF5252),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(station.id) },
                                        modifier = Modifier.size(36.dp).testTag("fav_button_${station.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (station.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Favoriye Ekle",
                                            tint = if (station.isFavorite) LcdAmber else Color(0xFF8B949E),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddStationDialog(
            initialFrequency = radioState.currentFrequency,
            onAddStation = { name, freq, genre, location ->
                viewModel.addCustomStation(name, freq, genre, location)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}
