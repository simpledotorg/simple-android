package org.simple.clinic.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.common.ui.theme.SimpleTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FilledButtonWithFrame(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    content: @Composable () -> Unit,
) {
  Box(
      modifier = Modifier
          .then(modifier)
          .background(SimpleTheme.colors.material.primaryVariant)
          .padding(12.dp)
          .navigationBarsPadding()
          .imePadding()
  ) {
    FilledButton(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { testTagsAsResourceId = true }
            .testTag(testTag),
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
