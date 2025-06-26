package org.simple.clinic.home.overdue.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.home.overdue.PendingListState

@Composable
fun OverdueSectionFooter(
    modifier: Modifier = Modifier,
    pendingListState: PendingListState,
    onClick: () -> Unit,
) {
  val buttonTextResource = when (pendingListState) {
    PendingListState.SEE_ALL -> R.string.overdue_pending_list_button_see_less
    PendingListState.SEE_LESS -> R.string.overdue_pending_list_button_see_all
  }

  TextButton(
      onClick = onClick,
      modifier = modifier
          .fillMaxWidth()
          .heightIn(min = 48.dp)
  ) {
    Text(text = stringResource(id = buttonTextResource).uppercase())
  }
}
