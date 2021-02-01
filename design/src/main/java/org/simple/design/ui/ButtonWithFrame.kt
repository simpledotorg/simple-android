package org.simple.design.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.design.ui.theme.SimpleTheme

@Composable
fun ButtonWithFrame(
    text: String,
    @DrawableRes iconRes: Int? = null,
    onClick: () -> Unit
) {
  Surface(
      modifier = Modifier.fillMaxWidth(),
      color = SimpleTheme.colors.primaryVariant,
  ) {
    Button(modifier = Modifier.padding(8.dp), onClick = onClick) {
      if (iconRes != null) {
        Image(bitmap = imageResource(id = iconRes), contentDescription = null)
      }

      Text(text = text, style = SimpleTheme.typography.buttonBig)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun ButtonWithFramePreview() {
  SimpleTheme {
    ButtonWithFrame(text = "DONE") {
      /* Handle Click */
    }
  }
}
