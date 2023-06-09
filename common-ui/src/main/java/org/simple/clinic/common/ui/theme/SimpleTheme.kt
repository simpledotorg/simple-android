package org.simple.clinic.common.ui.theme

import android.content.Context
import android.content.res.TypedArray
import androidx.compose.material.Colors
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Density
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
fun SimpleInverseTheme(content: @Composable () -> Unit) {
  SimpleThemeAdapter {
    val colors =
      SimpleTheme.colors.copy(
        material =
          SimpleTheme.colors.material.copy(
            primary = colorResource(id = R.color.simple_light_blue_100),
            onPrimary = colorResource(id = R.color.simple_light_blue_500)
          )
      )

    BaseSimpleTheme(colors = colors, typography = SimpleTheme.typography, content = content)
  }
}

@Composable
fun SimpleGreenTheme(content: @Composable () -> Unit) {
  SimpleThemeAdapter {
    val colors =
      SimpleTheme.colors.copy(
        material =
          SimpleTheme.colors.material.copy(
            primary = colorResource(id = R.color.simple_green_500),
            onPrimary = colorResource(id = R.color.white)
          )
      )

    BaseSimpleTheme(colors = colors, typography = SimpleTheme.typography, content = content)
  }
}

@Composable
fun SimpleRedTheme(content: @Composable () -> Unit) {
  SimpleThemeAdapter {
    val colors =
      SimpleTheme.colors.copy(
        material =
          SimpleTheme.colors.material.copy(
            primary = colorResource(id = R.color.simple_red_500),
            onPrimary = colorResource(id = R.color.white)
          )
      )

    BaseSimpleTheme(colors = colors, typography = SimpleTheme.typography, content = content)
  }
}

@Composable
fun SimpleRedInverseTheme(content: @Composable () -> Unit) {
  SimpleThemeAdapter {
    val colors =
      SimpleTheme.colors.copy(
        material =
          SimpleTheme.colors.material.copy(
            primary = colorResource(id = R.color.simple_red_100),
            onPrimary = colorResource(id = R.color.simple_red_500)
          )
      )

    BaseSimpleTheme(colors = colors, typography = SimpleTheme.typography, content = content)
  }
}

@Composable
private fun SimpleThemeAdapter(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val layoutDirection = LocalLayoutDirection.current
  val density = LocalDensity.current

  val materialThemeParameters = createMdcTheme(context, layoutDirection)

  requireNotNull(materialThemeParameters.colors)
  requireNotNull(materialThemeParameters.typography)
  requireNotNull(materialThemeParameters.shapes)

  val simpleThemeParameters =
    context.obtainStyledAttributes(R.styleable.SimpleThemeAttrs).use { ta ->
      val colors = parseSimpleColors(ta, materialThemeParameters.colors!!)
      val typography =
        parseSimpleTypography(context, ta, density, materialThemeParameters.typography!!)

      SimpleThemeParameters(colors = colors, typography = typography)
    }

  BaseSimpleTheme(
    content = content,
    colors = simpleThemeParameters.colors,
    typography = simpleThemeParameters.typography
  )
}

@Composable
private fun parseSimpleColors(ta: TypedArray, materialColors: Colors) =
  SimpleColors(
    toolbarPrimary = ta.parseColor(R.styleable.SimpleThemeAttrs_colorToolbarPrimary),
    toolbarPrimaryVariant = ta.parseColor(R.styleable.SimpleThemeAttrs_colorToolbarPrimaryVariant),
    onToolbarPrimary = ta.parseColor(R.styleable.SimpleThemeAttrs_colorOnToolbarPrimary),
    material = materialColors
  )

@Composable
private fun parseSimpleTypography(
  context: Context,
  ta: TypedArray,
  density: Density,
  materialTypography: Typography
): SimpleTypography {
  val typography =
    materialTypography.copy(
      h1 =
        materialTypography.h1.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      h2 =
        materialTypography.h2.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      h3 =
        materialTypography.h3.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      h4 =
        materialTypography.h4.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      h5 =
        materialTypography.h5.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      h6 =
        materialTypography.h6.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      subtitle1 =
        materialTypography.subtitle1.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      subtitle2 =
        materialTypography.subtitle2.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      body1 =
        materialTypography.body1.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      body2 =
        materialTypography.body2.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      button =
        materialTypography.button.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      caption =
        materialTypography.caption.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        ),
      overline =
        materialTypography.overline.copy(
          platformStyle = platformTextStyle,
          lineHeightStyle = lineHeightStyle
        )
    )

  return SimpleTypography(
    h5Numeric =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceHeadline5Numeric),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    h6Numeric =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceHeadline6Numeric),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    subtitle1Medium =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceSubtitle1Medium),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    body0 =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody0),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    body0Medium =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody0Medium),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    body0Numeric =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody0Numeric),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    body1Numeric =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody1Numeric),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    body2Numeric =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody2Numeric),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    body2Bold =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceBody2Bold),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    buttonBig =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceButtonBig),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    tag =
      parseTextAppearance(
          context,
          ta.getResourceIdOrThrow(R.styleable.SimpleThemeAttrs_textAppearanceTag),
          density,
          false,
          null
        )
        .copy(platformStyle = platformTextStyle, lineHeightStyle = lineHeightStyle),
    material = typography
  )
}

@Composable
private fun BaseSimpleTheme(
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

private val platformTextStyle = PlatformTextStyle(includeFontPadding = false)

private val lineHeightStyle =
  LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)
