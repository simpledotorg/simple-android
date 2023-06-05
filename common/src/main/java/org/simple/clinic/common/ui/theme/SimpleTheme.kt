package org.simple.clinic.common.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import com.google.accompanist.themeadapter.core.parseColor
import com.google.accompanist.themeadapter.core.parseTextAppearance
import com.google.accompanist.themeadapter.material.createMdcTheme
import org.simple.clinic.common.R

@Composable
fun SimpleTheme(content: @Composable () -> Unit) {
  SimpleThemeAdapter(content = content)
}

@Composable
private fun SimpleThemeAdapter(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val layoutDirection = LocalLayoutDirection.current
  val density = LocalDensity.current

  val materialThemeParameters = createMdcTheme(context, layoutDirection)

  val simpleThemeParameters =
    context.obtainStyledAttributes(R.styleable.SimpleThemeAttrs).use { ta ->
      val colors =
        SimpleColors(
          toolbarPrimary = ta.parseColor(R.styleable.SimpleThemeAttrs_colorToolbarPrimary),
          toolbarPrimaryVariant =
            ta.parseColor(R.styleable.SimpleThemeAttrs_colorToolbarPrimaryVariant),
          onToolbarPrimary = ta.parseColor(R.styleable.SimpleThemeAttrs_colorOnToolbarPrimary),
          material = materialThemeParameters.colors ?: MaterialTheme.colors
        )

      val typography =
        SimpleTypography(
          h5Numeric =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceHeadline5Numeric),
              density,
              false,
              null
            ),
          h6Numeric =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceHeadline6Numeric),
              density,
              false,
              null
            ),
          subtitle1Medium =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceSubtitle1Medium),
              density,
              false,
              null
            ),
          body0 =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody0),
              density,
              false,
              null
            ),
          body0Medium =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody0Medium),
              density,
              false,
              null
            ),
          body0Numeric =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody0Numeric),
              density,
              false,
              null
            ),
          body1Numeric =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody1Numeric),
              density,
              false,
              null
            ),
          body2Numeric =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody2Numeric),
              density,
              false,
              null
            ),
          body2Bold =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody2Bold),
              density,
              false,
              null
            ),
          buttonBig =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceButtonBig),
              density,
              false,
              null
            ),
          tag =
            parseTextAppearance(
              context,
              ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceTag),
              density,
              false,
              null
            ),
          material = materialThemeParameters.typography ?: Typography()
        )

      SimpleThemeParameters(colors = colors, typography = typography)
    }

  SimpleTheme(
    content = content,
    colors = simpleThemeParameters.colors,
    typography = simpleThemeParameters.typography
  )
}

@Composable
private fun SimpleTheme(
  content: @Composable () -> Unit,
  colors: SimpleColors,
  typography: SimpleTypography
) {
  MaterialTheme(colors = colors.material, typography = typography.material) {
    CompositionLocalProvider(
      LocalSimpleColors provides colors,
      LocalSimpleTypography provides typography
    ) {
      content()
    }
  }
}

object SimpleTheme {

  val colors: SimpleColors
    @Composable @ReadOnlyComposable get() = LocalSimpleColors.current

  val typography: SimpleTypography
    @Composable @ReadOnlyComposable get() = LocalSimpleTypography.current

  val shapes: Shapes
    @Composable @ReadOnlyComposable get() = MaterialTheme.shapes
}

private data class SimpleThemeParameters(
  val colors: SimpleColors,
  val typography: SimpleTypography
)
