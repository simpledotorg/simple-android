package org.simple.clinic.home.overdue.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle
import java.util.UUID

@Composable
fun OverdueScreenView(
    showDownloadAndShareButton: Boolean,
    showSelectedOverdueCountView: Boolean,
    showEmptyListView: Boolean,
    showLoader: Boolean,
    uiModels: List<OverdueUiModel>,
    selectedOverdueCount: Int,
    onCall: (UUID) -> Unit,
    onOpen: (UUID) -> Unit,
    onToggleSelection: (UUID) -> Unit,
    onSearch: () -> Unit,
    onToggleSection: (OverdueAppointmentSectionTitle) -> Unit,
    onToggleFooter: () -> Unit,
    onClearSelected: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,

    ) {
  Scaffold(
      bottomBar = {
        if (showDownloadAndShareButton) {
          OverdueScreenBottomActionView(
              showSelectedOverdueCountView = showSelectedOverdueCountView,
              selectedOverdueCount = selectedOverdueCount,
              onDownload = onDownload,
              onShare = onShare,
              onClearSelected = onClearSelected
          )
        }
      }

  ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
      if (showLoader) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      }

      OverdueAppointmentListItem(
          modifier = Modifier.fillMaxSize(),
          uiModels = uiModels,
          onCallClicked = onCall,
          onRowClicked = onOpen,
          onCheckboxClicked = onToggleSelection,
          onSearch = onSearch,
          onSectionHeaderClick = onToggleSection,
          onSectionFooterClick = onToggleFooter
      )

      if (showEmptyListView) {
        OverdueScreenEmptyView()
      }
    }
  }
}


@Preview(showBackground = true)
@Composable
fun OverdueScreenEmptyViewPreview(modifier: Modifier = Modifier) {
  SimpleTheme {
    OverdueScreenView(
        showDownloadAndShareButton = true,
        showSelectedOverdueCountView = false,
        showEmptyListView = false,
        showLoader = false,
        uiModels = emptyList(),
        selectedOverdueCount = 0,
        onCall = {},
        onOpen = {},
        onToggleSelection = {},
        onSearch = {},
        onToggleSection = {},
        onToggleFooter = {},
        onClearSelected = {},
        onDownload = {},
        onShare = {}
    )
  }
}

@Preview(showBackground = true)
@Composable
fun OverdueScreenLoaderViewPreview(modifier: Modifier = Modifier) {
  SimpleTheme {
    OverdueScreenView(
        showDownloadAndShareButton = false,
        showSelectedOverdueCountView = false,
        showEmptyListView = false,
        showLoader = true,
        uiModels = emptyList(),
        selectedOverdueCount = 0,
        onCall = {},
        onOpen = {},
        onToggleSelection = {},
        onSearch = {},
        onToggleSection = {},
        onToggleFooter = {},
        onClearSelected = {},
        onDownload = {},
        onShare = {}
    )
  }
}
