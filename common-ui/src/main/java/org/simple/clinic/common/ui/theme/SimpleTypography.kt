package org.simple.clinic.common.ui.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

@Immutable
data class SimpleTypography(
  val h5Numeric: TextStyle = TextStyle.Default,
  val h6Numeric: TextStyle = TextStyle.Default,
  val subtitle1Medium: TextStyle = TextStyle.Default,
  val body0: TextStyle = TextStyle.Default,
  val body0Medium: TextStyle = TextStyle.Default,
  val body0Numeric: TextStyle = TextStyle.Default,
  val body1Numeric: TextStyle = TextStyle.Default,
  val body2Numeric: TextStyle = TextStyle.Default,
  val body2Bold: TextStyle = TextStyle.Default,
  val buttonBig: TextStyle = TextStyle.Default,
  val tag: TextStyle = TextStyle.Default,
  val material: Typography = Typography()
)

internal val LocalSimpleTypography = staticCompositionLocalOf { SimpleTypography() }
