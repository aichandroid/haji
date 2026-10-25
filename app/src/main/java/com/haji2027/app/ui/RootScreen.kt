package com.haji2027.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.app.di.AppContainer
import com.haji2027.core.designsystem.theme.BackgroundSoftSky
import com.haji2027.core.designsystem.theme.BorderSubtle
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.PrimaryTrustBlue
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.feature.guide.detail.GuideDetailScreen
import com.haji2027.feature.guide.detail.GuideDetailViewModel
import com.haji2027.feature.guide.doa.DoaReadingViewModel
import com.haji2027.feature.guide.doa.DoaScreen
import com.haji2027.feature.guide.home.HomeScreen
import com.haji2027.feature.guide.home.HomeViewModel
import com.haji2027.feature.places.ui.compass.CompassScreen
import com.haji2027.feature.places.ui.compass.CompassViewModel
import com.haji2027.feature.places.ui.list.PlacesListScreen
import com.haji2027.feature.places.ui.list.PlacesListViewModel
import com.haji2027.feature.settings.purge.PurgeViewModel
import com.haji2027.feature.settings.ui.OtaViewModel
import com.haji2027.feature.settings.ui.SettingsScreen
import com.haji2027.feature.tracking.ui.TrackingViewModel
import com.haji2027.feature.tracking.ui.main.TrackingMainScreen
import java.io.File

/**
 * Layar utama (App Shell) yang mengintegrasikan seluruh fitur aplikasi dengan
 * Bilah Navigasi Bawah Ramah Lansia (WCAG AAA, target sentuh >= 56dp).
 */
@Composable
fun RootScreen(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var currentTab by remember { mutableStateOf(AppTab.GUIDE) }
    var guideRoute by remember { mutableStateOf<GuideRoute>(GuideRoute.Home) }
    var placesRoute by remember { mutableStateOf<PlacesRoute>(PlacesRoute.List) }

    // Inisialisasi ViewModels menggunakan container dependensi
    val homeViewModel = remember {
        HomeViewModel(
            guideRepository = container.guideRepository,
            checklistRepository = container.checklistRepository,
            calendarEngine = container.hijriCalendarEngine
        )
    }

    val placesListViewModel = remember {
        PlacesListViewModel(
            placesRepository = container.placesRepository,
            averagingEngine = container.gpsAveragingEngine,
            locationProvider = container.locationSampleProvider
        )
    }

    val trackingViewModel = remember {
        TrackingViewModel(
            apiClient = container.trackingApiClient,
            authManager = container.authManager,
            trackingController = container.trackingController,
            syncManager = container.syncManager
        )
    }

    val otaViewModel = remember {
        OtaViewModel(
            otaEngine = container.otaEngine,
            dualLayerManager = container.dualLayerContentManager,
            contentDir = File(context.filesDir, "content")
        )
    }

    val purgeViewModel = remember {
        PurgeViewModel(dataPurgeManager = container.dataPurgeManager)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSoftSky,
        bottomBar = {
            ElderlyBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                    // Reset internal routes when tab switches
                    if (tab == AppTab.GUIDE) guideRoute = GuideRoute.Home
                    if (tab == AppTab.PLACES) placesRoute = PlacesRoute.List
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                AppTab.GUIDE -> {
                    when (val route = guideRoute) {
                        is GuideRoute.Home -> {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToGuide = { stepId ->
                                    guideRoute = GuideRoute.Detail(stepId)
                                },
                                onNavigateToPlaces = {
                                    currentTab = AppTab.PLACES
                                    placesRoute = PlacesRoute.List
                                },
                                onNavigateToTracking = {
                                    currentTab = AppTab.TRACKING
                                },
                                onNavigateToSettings = {
                                    currentTab = AppTab.SETTINGS
                                }
                            )
                        }

                        is GuideRoute.Detail -> {
                            val detailViewModel = remember(route.stepId) {
                                GuideDetailViewModel(
                                    dayId = route.stepId,
                                    guideRepository = container.guideRepository,
                                    checklistRepository = container.checklistRepository
                                )
                            }
                            GuideDetailScreen(
                                viewModel = detailViewModel,
                                onNavigateBack = { guideRoute = GuideRoute.Home },
                                onNavigateToDoa = { doaId ->
                                    guideRoute = GuideRoute.Doa(doaId)
                                }
                            )
                        }

                        is GuideRoute.Doa -> {
                            val readingId = route.doaId ?: "doa_arafah_wukuf"
                            val doaViewModel = remember(readingId) {
                                DoaReadingViewModel(
                                    readingId = readingId,
                                    guideRepository = container.guideRepository
                                )
                            }
                            DoaScreen(
                                viewModel = doaViewModel,
                                onNavigateBack = { guideRoute = GuideRoute.Home }
                            )
                        }
                    }
                }

                AppTab.PLACES -> {
                    when (val route = placesRoute) {
                        is PlacesRoute.List -> {
                            PlacesListScreen(
                                viewModel = placesListViewModel,
                                onNavigateToCompass = { placeId ->
                                    placesRoute = PlacesRoute.Compass(placeId)
                                },
                                onBackClick = { currentTab = AppTab.GUIDE }
                            )
                        }

                        is PlacesRoute.Compass -> {
                            val compassViewModel = remember(route.placeId) {
                                CompassViewModel(
                                    targetPlaceId = route.placeId,
                                    placesRepository = container.placesRepository,
                                    navigationEngine = container.compassNavigationEngine,
                                    locationProvider = container.locationSampleProvider,
                                    sensorProvider = container.orientationSensorProvider
                                )
                            }
                            CompassScreen(
                                viewModel = compassViewModel,
                                onBackClick = { placesRoute = PlacesRoute.List }
                            )
                        }
                    }
                }

                AppTab.TRACKING -> {
                    TrackingMainScreen(viewModel = trackingViewModel)
                }

                AppTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = otaViewModel,
                        purgeViewModel = purgeViewModel
                    )
                }
            }
        }
    }
}

/**
 * Bilah navigasi bawah khusus lansia dengan target sentuh >= 56dp
 * dan pembedaan warna kontras tinggi (WCAG AAA).
 */
@Composable
private fun ElderlyBottomNavigationBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp),
        color = SurfaceWhite
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalDivider(
                thickness = 1.dp,
                color = BorderSubtle
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val tabColor = if (isSelected) PrimaryDarkNavy else MutedTextSlate

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(HajjDimens.MinTouchTargetSize)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) PrimaryTrustBlue.copy(alpha = 0.12f)
                                else SurfaceWhite
                            )
                            .clickable { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.contentDescription,
                                tint = tabColor,
                                modifier = Modifier.size(26.dp)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = tab.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = tabColor
                            )
                        }
                    }
                }
            }
        }
    }
}
