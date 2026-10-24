package com.haji2027.feature.guide.doa

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.BackgroundSoftSky
import com.haji2027.core.designsystem.theme.BorderSubtle
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.PrimaryTrustBlue
import com.haji2027.core.designsystem.theme.SurfaceWhite

/**
 * Layar SCR-03: Bacaan Doa Multilingual (Arab, Latin, Terjemahan).
 * Dilengkapi pengatur ukuran teks ramah lansia (TextSizeScale) dan rujukan dalil terpercaya.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoaScreen(
    viewModel: DoaReadingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val multiplier = uiState.textSizeScale.multiplier

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSoftSky,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.reading?.title ?: "Bacaan Doa",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(HajjDimens.MinTouchTargetSize)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Panduan",
                            tint = PrimaryDarkNavy
                        )
                    }
                },
                actions = {
                    // Tombol Toggle Ukuran Huruf (Aksesibilitas Lansia)
                    OutlinedButton(
                        onClick = { viewModel.toggleTextSize() },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PrimaryTrustBlue)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Ubah Ukuran Teks",
                                tint = PrimaryTrustBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = uiState.textSizeScale.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTrustBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryDarkNavy)
            }
        } else {
            val reading = uiState.reading
            if (reading == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Doa tidak ditemukan.",
                        fontSize = 16.sp,
                        color = MutedTextSlate
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(HajjDimens.SpaceMedium),
                    verticalArrangement = Arrangement.spacedBy(HajjDimens.SpaceMedium)
                ) {
                    // 1. Kartu Teks Arab Utama
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(HajjDimens.CardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.5.dp, BorderSubtle),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(HajjDimens.SpaceLarge),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Teks Arab:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedTextSlate
                            )

                            Text(
                                text = reading.arabicText,
                                fontSize = (28f * multiplier).sp,
                                lineHeight = (46f * multiplier).sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDarkNavy,
                                fontFamily = FontFamily.Default,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth(),
                                style = androidx.compose.ui.text.TextStyle(
                                    textDirection = TextDirection.Rtl
                                )
                            )
                        }
                    }

                    // 2. Kartu Transliterasi Latin
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(HajjDimens.CardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderSubtle),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(HajjDimens.SpaceMedium),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Cara Membaca (Latin):",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTrustBlue
                            )

                            Text(
                                text = reading.latinTransliteration,
                                fontSize = (18f * multiplier).sp,
                                lineHeight = (26f * multiplier).sp,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryDarkNavy
                            )
                        }
                    }

                    // 3. Kartu Terjemahan Bahasa Indonesia
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(HajjDimens.CardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderSubtle),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(HajjDimens.SpaceMedium),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Artinya:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedTextSlate
                            )

                            Text(
                                text = reading.translationId,
                                fontSize = (17f * multiplier).sp,
                                lineHeight = (25f * multiplier).sp,
                                fontWeight = FontWeight.Normal,
                                color = PrimaryDarkNavy
                            )
                        }
                    }

                    // 4. Kartu Sumber Rujukan Dalil (muslim.or.id, rumaysho.com, almanhaj.or.id, yufid.com)
                    reading.sourceRef?.takeIf { it.isNotBlank() }?.let { ref ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = PrimaryTrustBlue.copy(alpha = 0.06f)
                            ),
                            border = BorderStroke(1.dp, PrimaryTrustBlue.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(HajjDimens.SpaceMedium),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PrimaryTrustBlue,
                                    modifier = Modifier.size(20.dp)
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Rujukan Sumber & Dalil:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryTrustBlue
                                    )
                                    Text(
                                        text = ref,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = PrimaryDarkNavy,
                                        lineHeight = 20.sp
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
