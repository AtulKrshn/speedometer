package com.orca.tracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = OrcaPrimary,
    onPrimary = OrcaOnPrimary,
    primaryContainer = OrcaContainer,
    onPrimaryContainer = OrcaOnContainer,

    secondary = OrcaSecondary,
    onSecondary = OrcaOnSecondary,
    secondaryContainer = OrcaContainerSecondary,
    onSecondaryContainer = OrcaOnContainerSecondary,

    tertiary = PolylineBlue,
    onTertiary = Color.White,

    error = OrcaError,
    onError = OrcaOnError,

    background = OrcaBackground,
    onBackground = OrcaOnBackground,

    surface = OrcaSurface,
    onSurface = OrcaOnSurface,

    outline = Color(0xFF79747E)
)

private val DarkColorScheme = darkColorScheme(
    primary = OrcaPrimaryDark,
    onPrimary = OrcaOnPrimaryDark,
    primaryContainer = OrcaContainerDark,
    onPrimaryContainer = OrcaOnContainerDark,

    secondary = OrcaSecondaryDark,
    onSecondary = OrcaOnSecondaryDark,
    secondaryContainer = OrcaContainerSecondaryDark,
    onSecondaryContainer = OrcaOnContainerSecondaryDark,

    tertiary = PolylineBlue,
    onTertiary = Color.Black,

    error = OrcaErrorDark,
    onError = OrcaOnErrorDark,

    background = OrcaBackgroundDark,
    onBackground = OrcaOnBackgroundDark,

    surface = OrcaSurfaceDark,
    onSurface = OrcaOnSurfaceDark,

    outline = Color(0xFF938F99)
)

@Composable
fun OrcaTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Uses default Material Typography for now to avoid errors
        content = content
    )
}