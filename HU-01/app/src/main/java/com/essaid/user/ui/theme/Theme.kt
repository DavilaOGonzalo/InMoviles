package com.essaid.user.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = EssAidPrimary,
    onPrimary = EssAidOnPrimary,
    primaryContainer = EssAidPrimaryContainer,
    onPrimaryContainer = EssAidOnPrimaryContainer,
    secondary = EssAidSecondary,
    secondaryContainer = EssAidSecondaryContainer,
    onSecondaryContainer = EssAidOnSecondaryContainer,
    background = EssAidBackground,
    onBackground = EssAidOnBackground,
    error = EssAidError,
    errorContainer = EssAidErrorContainer,
    onErrorContainer = EssAidOnErrorContainer,
)

private val DarkColors = darkColorScheme(
    primary = ColorTokens.DarkPrimary,
    onPrimary = ColorTokens.DarkOnPrimary,
    primaryContainer = ColorTokens.DarkPrimaryContainer,
    onPrimaryContainer = ColorTokens.DarkOnPrimaryContainer,
    secondary = ColorTokens.DarkSecondary,
    secondaryContainer = ColorTokens.DarkSecondaryContainer,
    onSecondaryContainer = ColorTokens.DarkOnSecondaryContainer,
    error = ColorTokens.DarkError,
    errorContainer = ColorTokens.DarkErrorContainer,
    onErrorContainer = ColorTokens.DarkOnErrorContainer,
)

@Composable
fun EssAidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}

private object ColorTokens {
    val DarkPrimary = EssAidPrimaryContainer
    val DarkOnPrimary = EssAidOnPrimaryContainer
    val DarkPrimaryContainer = Color(0xFF004D63)
    val DarkOnPrimaryContainer = EssAidPrimaryContainer
    val DarkSecondary = Color(0xFFB4CAD3)
    val DarkSecondaryContainer = Color(0xFF334A52)
    val DarkOnSecondaryContainer = Color(0xFFD0E6EF)
    val DarkError = Color(0xFFFFB4AB)
    val DarkErrorContainer = Color(0xFF93000A)
    val DarkOnErrorContainer = Color(0xFFFFDAD6)
}
