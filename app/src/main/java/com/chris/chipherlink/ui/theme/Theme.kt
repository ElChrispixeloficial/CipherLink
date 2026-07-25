package com.chris.chipherlink.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.chris.chipherlink.data.local.SecurePreferences

private val LightColorScheme = lightColorScheme(
    primary = CipherPrimaryLight,
    onPrimary = CipherOnPrimaryLight,
    primaryContainer = CipherPrimaryContainerLight,
    onPrimaryContainer = CipherOnPrimaryContainerLight,
    secondary = CipherSecondaryLight,
    onSecondary = CipherOnSecondaryLight,
    secondaryContainer = CipherSecondaryContainerLight,
    onSecondaryContainer = CipherOnSecondaryContainerLight,
    tertiary = CipherTertiaryLight,
    onTertiary = CipherOnTertiaryLight,
    tertiaryContainer = CipherTertiaryContainerLight,
    onTertiaryContainer = CipherOnTertiaryContainerLight,
    background = CipherBackgroundLight,
    onBackground = CipherOnBackgroundLight,
    surface = CipherSurfaceLight,
    onSurface = CipherOnSurfaceLight,
    surfaceVariant = CipherSurfaceVariantLight,
    onSurfaceVariant = CipherOnSurfaceVariantLight,
    error = CipherErrorLight,
    onError = CipherOnErrorLight,
    errorContainer = CipherErrorContainerLight,
    onErrorContainer = CipherOnErrorContainerLight,
    outline = CipherOutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = CipherPrimaryDark,
    onPrimary = CipherOnPrimaryDark,
    primaryContainer = CipherPrimaryContainerDark,
    onPrimaryContainer = CipherOnPrimaryContainerDark,
    secondary = CipherSecondaryDark,
    onSecondary = CipherOnSecondaryDark,
    secondaryContainer = CipherSecondaryContainerDark,
    onSecondaryContainer = CipherOnSecondaryContainerDark,
    tertiary = CipherTertiaryDark,
    onTertiary = CipherOnTertiaryDark,
    tertiaryContainer = CipherTertiaryContainerDark,
    onTertiaryContainer = CipherOnTertiaryContainerDark,
    background = CipherBackgroundDark,
    onBackground = CipherOnBackgroundDark,
    surface = CipherSurfaceDark,
    onSurface = CipherOnSurfaceDark,
    surfaceVariant = CipherSurfaceVariantDark,
    onSurfaceVariant = CipherOnSurfaceVariantDark,
    error = CipherErrorDark,
    onError = CipherOnErrorDark,
    errorContainer = CipherErrorContainerDark,
    onErrorContainer = CipherOnErrorContainerDark,
    outline = CipherOutlineDark
)

@Composable
fun ChipherlinkTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val securePreferences = SecurePreferences(context)
    val themeMode by securePreferences.themeMode.collectAsState()

    val isDarkTheme = when (themeMode) {
        SecurePreferences.THEME_DARK -> true
        SecurePreferences.THEME_LIGHT -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
