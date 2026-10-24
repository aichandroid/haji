package com.haji2027.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTrustBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryDarkNavy,
    onPrimaryContainer = SurfaceWhite,
    secondary = SecondarySky,
    onSecondary = PrimaryDarkNavy,
    background = BackgroundSoftSky,
    onBackground = PrimaryDarkNavy,
    surface = SurfaceWhite,
    onSurface = PrimaryDarkNavy,
    surfaceVariant = BackgroundSoftSky,
    onSurfaceVariant = MutedTextSlate,
    outline = BorderSubtle,
    error = StatusRedError,
    onError = SurfaceWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = SecondarySky,
    onPrimary = PrimaryDarkNavy,
    primaryContainer = PrimaryTrustBlue,
    onPrimaryContainer = SurfaceWhite,
    secondary = SecondarySky,
    onSecondary = PrimaryDarkNavy,
    background = PrimaryDarkNavy,
    onBackground = SurfaceWhite,
    surface = PrimaryDarkNavy,
    onSurface = SurfaceWhite,
    surfaceVariant = MutedTextSlate,
    onSurfaceVariant = BackgroundSoftSky,
    outline = BorderSubtle,
    error = StatusRedError,
    onError = SurfaceWhite
)

@Composable
fun HajjTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HajjTypography,
        content = content
    )
}
