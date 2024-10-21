package org.simple.clinic.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun FilledButtonWithFrame(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
  Box(
      modifier = Modifier
          .then(modifier)
          .background(SimpleTheme.colors.material.primaryVariant)
          .padding(12.dp)
  ) {
    FilledButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
      content()
    }
  }
}

@Preview
@Composable
private fun FilledButtonWithFramePreview() {
  SimpleTheme {
    FilledButtonWithFrame(
        onClick = {
          // no-op
        }
    ) {
      Text(text = "DONE")
    }
  }
}
