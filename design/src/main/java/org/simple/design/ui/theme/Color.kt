package org.simple.design.ui.theme

import androidx.compose.ui.graphics.Color

val SimpleLightPalette = SimpleColors()

data class SimpleColors(
    val primary: Color = Color(0xFF0075EB),
    val primaryVariant: Color = Color(0xFFE0F0FF),
    val secondary: Color = Color(0xFF00B849),
    val secondaryVariant: Color = Color(0xFFE0FFED),
    val toolbarPrimary: Color = Color(0xFF0C3966),
    val toolbarPrimaryVariant: Color = Color(0xFF0A2E52),
    val onToolbarPrimary: Color = Color.White,
    val onToolbarPrimary72: Color = onToolbarPrimary.copy(alpha = 0.72f),
    val surface: Color = Color.White,
    val onSurface11: Color = surface.copy(alpha = 0.11f),
    val onSurface34: Color = surface.copy(alpha = 0.34f),
    val onSurface67: Color = surface.copy(alpha = 0.67f),
    val onSurface: Color = Color(0xFF2F363D),
    val error: Color = Color(0xFFFF3355),
    val onError: Color = Color.White,
    val scrim: Color = Color(0x85000000),
    val onBackground: Color = Color(0xFF2F363D),
    val isLight: Boolean = true
)
