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
import androidx.compose.material.TextButton as MaterialTextButton

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
    ButtonSize.Small -> 40.dp
    ButtonSize.Default -> 48.dp
    ButtonSize.Big -> 56.dp
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

@Composable
fun FilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    buttonSize: ButtonSize = ButtonSize.Default,
    content: @Composable RowScope.() -> Unit
) {
  val minHeight = when (buttonSize) {
    ButtonSize.Small -> 40.dp
    ButtonSize.Default -> 48.dp
    ButtonSize.Big -> 56.dp
  }

  androidx.compose.material.Button(
      modifier = modifier.defaultMinSize(
          minWidth = 64.dp,
          minHeight = minHeight
      ),
      onClick = onClick,
      enabled = enabled,
      border = null
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

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    buttonSize: ButtonSize = ButtonSize.Default,
    content: @Composable RowScope.() -> Unit
) {
  val minHeight = when (buttonSize) {
    ButtonSize.Small -> 40.dp
    ButtonSize.Default -> 48.dp
    ButtonSize.Big -> 56.dp
  }

  MaterialTextButton(
      modifier = modifier.defaultMinSize(
          minWidth = 64.dp,
          minHeight = minHeight
      ),
      onClick = onClick,
      enabled = enabled,
  ) {
    leadingIcon?.let {
      it()
      Spacer(modifier = Modifier.requiredWidth(8.dp))
    }
    ProvideTextStyle(value = SimpleTheme.typography.buttonBig) {
      content()
    }
    trailingIcon?.let {
      Spacer(modifier = Modifier.requiredWidth(8.dp))
      it()
    }
  }
}

sealed interface ButtonSize {
  data object Default : ButtonSize
  data object Big : ButtonSize
  data object Small : ButtonSize
}

@Preview(group = "OutlinedButton")
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

@Preview(group = "OutlinedButton")
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

@Preview(group = "OutlinedButton")
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

@Preview(group = "OutlinedButton")
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

@Preview(group = "FilledButton")
@Composable
private fun FilledButtonPreview() {
  SimpleTheme {
    FilledButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview(group = "FilledButton")
@Composable
private fun FilledButtonBigPreview() {
  SimpleTheme {
    FilledButton(
        modifier = Modifier.fillMaxWidth(),
        buttonSize = ButtonSize.Big,
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview(group = "FilledButton")
@Composable
private fun FilledButtonWithDifferentThemePreview() {
  SimpleRedTheme {
    FilledButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview(group = "FilledButton")
@Composable
private fun FilledButtonWithIconPreview() {
  SimpleTheme {
    FilledButton(
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

@Preview(group = "TextButton")
@Composable
private fun TextButtonPreview() {
  SimpleTheme {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview(group = "TextButton")
@Composable
private fun TextButtonSmallPreview() {
  SimpleTheme {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        buttonSize = ButtonSize.Small,
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview(group = "TextButton")
@Composable
private fun TextButtonBigPreview() {
  SimpleTheme {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        buttonSize = ButtonSize.Big,
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview(group = "TextButton")
@Composable
private fun TextButtonWithDifferentThemePreview() {
  SimpleRedTheme {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview(group = "TextButton")
@Composable
private fun TextButtonWithLeadingIconPreview() {
  SimpleTheme {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
          Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        },
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}

@Preview(group = "TextButton")
@Composable
private fun TextButtonWithTrailingIconPreview() {
  SimpleTheme {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
          Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        },
        onClick = { /*no-op*/ }
    ) {
      Text(text = "BUTTON")
    }
  }
}
