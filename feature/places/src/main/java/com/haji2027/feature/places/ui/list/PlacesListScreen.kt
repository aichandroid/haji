package com.haji2027.feature.places.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.AccentHajjGreen
import com.haji2027.core.designsystem.theme.BackgroundSoftSky
import com.haji2027.core.designsystem.theme.BorderSubtle
import com.haji2027.core.designsystem.theme.CompassAccuracyFair
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.PrimaryTrustBlue
import com.haji2027.core.designsystem.theme.SecondarySky
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.core.model.PlaceCategory
import com.haji2027.core.model.SavedPlace
import com.haji2027.feature.places.domain.BearingCalculator
import com.haji2027.feature.places.ui.components.AddPlaceDialog

/**
 * Layar utama daftar tempat tersimpan jemaah ("Tempat Saya").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesListScreen(
    viewModel: PlacesListViewModel,
    onNavigateToCompass: (placeId: String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tempat Saya",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                            tint = PrimaryDarkNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundSoftSky
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Raksasa: Tandai Tempat Saya
            Button(
                onClick = { viewModel.openAddPlaceDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = AccentHajjGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(HajjDimens.CardCornerRadius),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocationAlt,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "TANDAI TEMPAT INI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceWhite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips Kategori
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("Semua", fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryTrustBlue.copy(alpha = 0.2f),
                        selectedLabelColor = PrimaryDarkNavy
                    )
                )
                val categoryFilters = listOf(
                    PlaceCategory.DOOR to "Pintu",
                    PlaceCategory.HOTEL to "Hotel",
                    PlaceCategory.CAMP to "Tenda",
                    PlaceCategory.BUS to "Bus"
                )
                categoryFilters.forEach { (cat, label) ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = { viewModel.setCategoryFilter(cat) },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryTrustBlue.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryDarkNavy
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Daftar Tempat Tersimpan
            if (uiState.places.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(BorderSubtle.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MutedTextSlate,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum Ada Tempat Tersimpan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ketuk tombol hijau di atas untuk menandai pintu masuk masjid, hotel, atau tenda Mina Anda.",
                            fontSize = 14.sp,
                            color = MutedTextSlate,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.places, key = { it.placeId }) { place ->
                        PlaceItemCard(
                            place = place,
                            currentGps = uiState.currentGpsLocation?.let { Pair(it.latitude, it.longitude) },
                            onNavigateClick = { onNavigateToCompass(place.placeId) },
                            onDeleteClick = { viewModel.deletePlace(place.placeId) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddPlaceDialog(
            isSampling = uiState.isSamplingGps,
            samplingProgress = uiState.samplingProgress,
            samplingTarget = uiState.samplingTarget,
            latestAccuracy = uiState.latestSamplingAccuracy,
            sampledLocation = uiState.sampledLocation,
            errorMessage = uiState.samplingErrorMessage,
            onRetrySampling = { viewModel.retrySampling() },
            onSavePlace = { name, cat, notes, loc ->
                viewModel.saveNewPlace(name, cat, notes, loc)
            },
            onDismiss = { viewModel.closeAddPlaceDialog() }
        )
    }
}

@Composable
private fun PlaceItemCard(
    place: SavedPlace,
    currentGps: Pair<Double, Double>?,
    onNavigateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikon Kategori
                val (catIcon, catColor) = getCategoryIconAndColor(place.category)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = catIcon,
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = getCategoryLabel(place.category),
                        fontSize = 13.sp,
                        color = catColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Tempat",
                        tint = MutedTextSlate
                    )
                }
            }

            if (!place.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = place.notes,
                    fontSize = 14.sp,
                    color = MutedTextSlate,
                    lineHeight = 20.sp
                )
            }

            // Estimasi Jarak Garis Lurus jika GPS tersedia
            if (currentGps != null) {
                val distanceMeters = BearingCalculator.calculateDistanceMeters(
                    lat1 = currentGps.first,
                    lon1 = currentGps.second,
                    lat2 = place.latitude,
                    lon2 = place.longitude
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryTrustBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Jarak garis lurus: ${BearingCalculator.formatDistance(distanceMeters)}",
                        fontSize = 13.sp,
                        color = PrimaryDarkNavy,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tombol Menuju Tempat
            Button(
                onClick = onNavigateClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTrustBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HajjDimens.MinTouchTargetSize),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MENUJU TEMPAT (PANAH) >>",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceWhite
                )
            }
        }
    }
}

private fun getCategoryIconAndColor(category: PlaceCategory): Pair<ImageVector, androidx.compose.ui.graphics.Color> {
    return when (category) {
        PlaceCategory.DOOR -> Pair(Icons.Default.DoorFront, PrimaryTrustBlue)
        PlaceCategory.HOTEL -> Pair(Icons.Default.Hotel, SecondarySky)
        PlaceCategory.CAMP -> Pair(Icons.Default.Tent, AccentHajjGreen)
        PlaceCategory.BUS -> Pair(Icons.Default.DirectionsBus, CompassAccuracyFair)
        PlaceCategory.OTHER -> Pair(Icons.Default.Place, MutedTextSlate)
    }
}

private fun getCategoryLabel(category: PlaceCategory): String {
    return when (category) {
        PlaceCategory.DOOR -> "Pintu Masjid"
        PlaceCategory.HOTEL -> "Hotel Penginapan"
        PlaceCategory.CAMP -> "Tenda Armuzna"
        PlaceCategory.BUS -> "Terminal / Halte Bus"
        PlaceCategory.OTHER -> "Tempat Lainnya"
    }
}
