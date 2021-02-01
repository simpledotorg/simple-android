package org.simple.design.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.staticAmbientOf

@Composable
fun SimpleTheme(content: @Composable () -> Unit) {
  // TODO: Prepare dark palette
  val simpleColors = SimpleLightPalette
  val simpleTypography = SimpleTypography()

  Providers(
      ContextLocalSimpleColors provides simpleColors,
      ContextLocalSimpleTypography provides simpleTypography
  ) {
    MaterialTheme(
        colors = debugColors(),
        content = content,
    )
  }
}

object SimpleTheme {

  @Composable
  val colors: SimpleColors
    get() = ContextLocalSimpleColors.current

  @Composable
  val typography: SimpleTypography
    get() = ContextLocalSimpleTypography.current
}

private val ContextLocalSimpleColors = staticAmbientOf<SimpleColors> {
  error("No SimpleColors provided")
}

private val ContextLocalSimpleTypography = staticAmbientOf<SimpleTypography> {
  error("No SimpleTypography provided")
}

private fun debugColors(): Colors {
  return lightColors()
}
