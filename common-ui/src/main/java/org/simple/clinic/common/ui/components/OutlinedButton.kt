package org.simple.clinic.common.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.Icon
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.common.ui.theme.SimpleRedTheme
import org.simple.clinic.common.ui.theme.SimpleTheme
import androidx.compose.material.OutlinedButton as MaterialOutlinedButton

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    buttonSize: ButtonSize = ButtonSize.Default,
    content: @Composable RowScope.() -> Unit
) {
  val minHeight = when (buttonSize) {
    ButtonSize.Big -> 56.dp
    ButtonSize.Default -> 48.dp
  }

  MaterialOutlinedButton(
      modifier = modifier.defaultMinSize(
          minWidth = 64.dp,
          minHeight = minHeight
      ),
      onClick = onClick,
      enabled = enabled,
      border = if (enabled) {
        BorderStroke(width = 1.dp, SimpleTheme.colors.material.primary)
      } else {
        BorderStroke(width = 1.dp, SimpleTheme.colors.material.onSurface.copy(alpha = 0.16f))
      }
  ) {
    icon?.let {
      it()
      Spacer(modifier = Modifier.requiredWidth(8.dp))
    }
    ProvideTextStyle(value = SimpleTheme.typography.buttonBig) {
      content()
    }
  }
}

sealed interface ButtonSize {
  data object Default : ButtonSize
  data object Big : ButtonSize
}

@Preview
@Composable
private fun OutlinedButtonPreview() {
  SimpleTheme {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview
@Composable
private fun OutlinedButtonBigPreview() {
  SimpleTheme {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        buttonSize = ButtonSize.Big,
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview
@Composable
private fun OutlinedButtonWithDifferentThemePreview() {
  SimpleRedTheme {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview
@Composable
private fun OutlinedButtonWithIconPreview() {
  SimpleTheme {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        icon = {
          Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        },
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}
