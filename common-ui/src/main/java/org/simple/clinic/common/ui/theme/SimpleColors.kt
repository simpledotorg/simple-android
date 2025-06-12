package org.simple.clinic.common.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SimpleColors(
    val toolbarPrimary: Color = Color.Unspecified,
    val toolbarPrimaryVariant: Color = Color.Unspecified,
    val onToolbarPrimary: Color = Color.Unspecified,
    val onToolbarPrimary72: Color = onToolbarPrimary.copy(alpha = 0.72f),
    val material: Colors = lightColors(),
    val onSurface67: Color = material.onSurface.copy(alpha = 0.67f),
    val onSurface11: Color = material.onSurface.copy(alpha = 0.11f)
)

internal val LocalSimpleColors = staticCompositionLocalOf { SimpleColors() }
