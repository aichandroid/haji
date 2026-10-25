package com.haji2027.feature.places.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.AccentHajjGreen
import com.haji2027.core.designsystem.theme.BackgroundSoftSky
import com.haji2027.core.designsystem.theme.BorderSubtle
import com.haji2027.core.designsystem.theme.CompassAccuracyGood
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.PrimaryTrustBlue
import com.haji2027.core.designsystem.theme.StatusRedError
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.core.model.PlaceCategory
import com.haji2027.feature.places.domain.AveragedLocation

/**
 * Dialog penandaan tempat tersimpan baru dengan proses stabilisasi GPS Multi-Sampling (FR-PLACE-001, FR-PLACE-002).
 */
@Composable
fun AddPlaceDialog(
    isSampling: Boolean,
    samplingProgress: Int,
    samplingTarget: Int,
    latestAccuracy: Float?,
    sampledLocation: AveragedLocation?,
    errorMessage: String?,
    onRetrySampling: () -> Unit,
    onSavePlace: (name: String, category: PlaceCategory, notes: String?, location: AveragedLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var placeName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PlaceCategory.DOOR) }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tandai Tempat Ini",
                color = PrimaryDarkNavy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Status Multi-Sampling GPS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundSoftSky)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        if (sampledLocation != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CompassAccuracyGood,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Titik GPS Terkunci!",
                                        color = CompassAccuracyGood,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Akurasi: ${sampledLocation.accuracyMeters.toInt()}m (${sampledLocation.validSampleCount} sampel valid)",
                                        color = MutedTextSlate,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else if (isSampling) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = null,
                                    tint = PrimaryTrustBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mengumpulkan sinyal GPS ($samplingProgress/$samplingTarget)...",
                                    color = PrimaryDarkNavy,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = {
                                    if (samplingTarget > 0) samplingProgress.toFloat() / samplingTarget else 0f
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = AccentHajjGreen,
                                trackColor = BorderSubtle
                            )
                            if (latestAccuracy != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Akurasi sampel terkini: ${latestAccuracy.toInt()}m",
                                    color = MutedTextSlate,
                                    fontSize = 12.sp
                                )
                            }
                        } else if (errorMessage != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = StatusRedError,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage,
                                    color = StatusRedError,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = onRetrySampling,
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Ulangi", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ulangi", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input Nama Tempat
                OutlinedTextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    label = { Text("Nama Tempat (Wajib)") },
                    placeholder = { Text("Contoh: Pintu 1 King Abdulaziz") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Kategori Tempat (Chips)
                Text(
                    text = "Kategori Tempat:",
                    color = PrimaryDarkNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf(
                        PlaceCategory.DOOR to "Pintu",
                        PlaceCategory.HOTEL to "Hotel",
                        PlaceCategory.CAMP to "Tenda",
                        PlaceCategory.BUS to "Bus"
                    )
                    categories.forEach { (cat, label) ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentHajjGreen.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryDarkNavy
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Catatan Opsional
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    placeholder = { Text("Contoh: Dekat tiang nomor 14, lantai dasar") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (sampledLocation != null && placeName.isNotBlank()) {
                        onSavePlace(
                            placeName.trim(),
                            selectedCategory,
                            notesText.trim().ifBlank { null },
                            sampledLocation
                        )
                    }
                },
                enabled = sampledLocation != null && placeName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentHajjGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HajjDimens.MinTouchTargetSize),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SIMPAN TEMPAT INI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceWhite
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "BATAL",
                    fontSize = 14.sp,
                    color = MutedTextSlate
                )
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
    )
}
