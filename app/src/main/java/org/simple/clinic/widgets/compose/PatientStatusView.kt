package org.simple.clinic.widgets.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun PatientStatusView(
    text: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colors.onError,
    textColor: Color = MaterialTheme.colors.onError
) {
  Card(
      modifier = modifier.fillMaxWidth(),
      backgroundColor = MaterialTheme.colors.error
  ) {
    Row(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.spacing_16)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8))
    ) {
      Icon(
          painter = icon,
          contentDescription = null,
          tint = iconTint
      )
      Text(
          text = text,
          style = MaterialTheme.typography.h6,
          color = textColor
      )
    }
  }
}

@Preview
@Composable
private fun PatientStatusDiedPreview() {
  SimpleTheme {
    PatientStatusView(
        text = stringResource(id = R.string.patient_status_died),
        icon = painterResource(id = R.drawable.ic_patient_dead_32dp)
    )
  }
}
