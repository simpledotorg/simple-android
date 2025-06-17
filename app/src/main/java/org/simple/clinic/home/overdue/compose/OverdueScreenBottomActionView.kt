package org.simple.clinic.home.overdue.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.FilledButton
import org.simple.clinic.common.ui.components.TextButton
import org.simple.clinic.common.ui.theme.SimpleGreenTheme
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun OverdueScreenBottomActionView(
    showSelectedOverdueCountView: Boolean,
    selectedOverdueCount: Int,
    onClearSelected: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
) {
  Column(
      modifier = Modifier
          .fillMaxWidth()
          .background(SimpleTheme.colors.material.primaryVariant)
          .padding(dimensionResource(R.dimen.spacing_12)),
  ) {
    if (showSelectedOverdueCountView) {
      OverdueSelectedItemsCountView(
          selectedOverdueCount = selectedOverdueCount,
          onClearSelected = onClearSelected
      )
    }

    DownloadAndShareButtonView(
        onDownload = onDownload,
        onShare = onShare
    )
  }
}

@Composable
fun OverdueSelectedItemsCountView(
    selectedOverdueCount: Int,
    onClearSelected: () -> Unit
) {
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = dimensionResource(R.dimen.spacing_12)),
      verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
        modifier = Modifier.weight(1f),
        text = stringResource(R.string.selected_overdue_count, selectedOverdueCount),
        style = SimpleTheme.typography.material.body1,
        color = SimpleTheme.colors.material.onBackground
    )
    TextButton(
        onClick = onClearSelected
    ) {
      Text(
          text = stringResource(R.string.selected_overdue_clear),
          style = SimpleTheme.typography.tag
      )
    }
  }
}

@Composable
fun DownloadAndShareButtonView(
    onDownload: () -> Unit,
    onShare: () -> Unit
) {

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_16))
  ) {
    FilledButton(
        modifier = Modifier.weight(1f),
        onClick = onDownload
    ) {
      Text(text = stringResource(R.string.overdue_download))
    }
    SimpleGreenTheme {
      FilledButton(
          modifier = Modifier.weight(1f),
          onClick = onShare
      ) {
        Text(text = stringResource(R.string.overdue_share))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun OverdueScreenBottomActionView_Preview() {
  SimpleTheme {
    OverdueScreenBottomActionView(
        showSelectedOverdueCountView = true,
        selectedOverdueCount = 10,
        onShare = {},
        onDownload = {},
        onClearSelected = {}
    )
  }
}

@Preview(showBackground = true)
@Composable
fun OverdueSelectedItemsCountView_Preview() {
  SimpleTheme {
    OverdueSelectedItemsCountView(
        selectedOverdueCount = 12,
        onClearSelected = {}
    )
  }
}

@Preview(showBackground = true)
@Composable
fun DownloadAndShareButtonView_Preview() {
  SimpleTheme {
    DownloadAndShareButtonView(
        onDownload = {},
        onShare = {},
    )
  }
}
