package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberCyan,
    onPrimary = ObsidianDark,
    primaryContainer = CyberCyanDark,
    onPrimaryContainer = Color.White,
    secondary = CyberPurple,
    onSecondary = Color.White,
    secondaryContainer = CyberPurpleDark,
    onSecondaryContainer = CyberPurpleLight,
    tertiary = CyberPink,
    onTertiary = Color.White,
    background = ObsidianDark,
    onBackground = TextPrimaryDark,
    surface = CyberSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = CyberBorder,
    error = CyberRose
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CyberCyanDark,
    onPrimary = Color.White,
    secondary = CyberPurple,
    onSecondary = Color.White,
    tertiary = CyberPink,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to futuristic dark theme
  dynamicColor: Boolean = false, // Preserve branded cyber aesthetics
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

