package org.simple.clinic.home.overdue.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.SearchButton

@Composable
fun OverduePatientSearchButton(
    modifier: Modifier = Modifier,
    onSearch: () -> Unit
) {
  SearchButton(
      onClick = onSearch,
      modifier = modifier
          .fillMaxWidth()
  ) {
    Text(text = stringResource(id = R.string.overdue_search_patient_name_or_village))
  }
}
