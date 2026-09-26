package com.food.freshkeeper.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FreshGreenPrimary,
    onPrimary = LightSurface,
    primaryContainer = FreshGreenContainer,
    onPrimaryContainer = FreshGreenDark,
    secondary = WarmOrange,
    onSecondary = LightSurface,
    secondaryContainer = WarmOrangeLight,
    onSecondaryContainer = WarmOrangeDark,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = LightBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = FreshGreenPrimary,
    onPrimary = DarkBackground,
    primaryContainer = FreshGreenDark,
    onPrimaryContainer = FreshGreenLight,
    secondary = WarmOrange,
    onSecondary = DarkBackground,
    secondaryContainer = WarmOrangeDark,
    onSecondaryContainer = WarmOrangeLight,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

@Composable
fun FoodKeeperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val bgArgb = colorScheme.background.toArgb()
                window.statusBarColor = bgArgb
                window.navigationBarColor = bgArgb
                window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgArgb))
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
