package com.mutissx.dicechallenge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DiceColorScheme = darkColorScheme(
    primary = DiceAccent,
    onPrimary = DiceOnAccent,
    primaryContainer = DiceSurfaceHigh,
    onPrimaryContainer = DiceAccent,
    secondary = DiceAccent,
    onSecondary = DiceOnAccent,
    background = DiceBackground,
    onBackground = DiceTextPrimary,
    surface = DiceSurface,
    onSurface = DiceTextPrimary,
    surfaceVariant = DiceSurfaceHigh,
    onSurfaceVariant = DiceTextSecondary,
    outline = DiceOutline,
    outlineVariant = DiceOutline,
    error = DiceError,
    onError = DiceOnError
)

@Composable
fun DICEChallengeTheme(
    content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DiceColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}