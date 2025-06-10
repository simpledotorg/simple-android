package org.simple.clinic.home.overdue.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun NoPendingPatients(modifier: Modifier = Modifier) {
  Card(
      modifier = modifier
          .fillMaxWidth()
          .padding(8.dp)
  ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
          painter = painterResource(id = R.drawable.ic_no_pending_patients_illustration),
          contentDescription = null
      )

      Text(
          modifier = Modifier.padding(top = 16.dp),
          text = stringResource(id = R.string.overdue_no_pending_patients),
          style = SimpleTheme.typography.body0Medium,
          color = SimpleTheme.colors.material.secondary
      )
    }
  }
}

@Preview
@Composable
private fun NoPendingPatientsPreview(modifier: Modifier = Modifier) {
  NoPendingPatients(modifier = modifier)
}
