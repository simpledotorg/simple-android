package org.simple.clinic.summary.nextappointment.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.ButtonSize
import org.simple.clinic.common.ui.components.TextButton
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun NextAppointment(
    modifier: Modifier = Modifier,
    appointmentDate: String,
    facilityName: String? = null,
    actionButtonText: String,
    onAction: () -> Unit,
) {
  Card(
      modifier = modifier,
      backgroundColor = MaterialTheme.colors.surface
  )
  {
    Column(
        modifier = Modifier.padding(
            start = dimensionResource(R.dimen.spacing_16),
            bottom = dimensionResource(R.dimen.spacing_12)
        )
    ) {

      Row(
          modifier = Modifier.padding(
              top = dimensionResource(R.dimen.spacing_4),
              end = dimensionResource(R.dimen.spacing_8)
          ),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8))
      ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.next_appointment_view_title),
            style = SimpleTheme.typography.subtitle1Medium,
            color = MaterialTheme.colors.onSurface,
        )

        TextButton(
            buttonSize = ButtonSize.ExtraSmall,
            onClick = onAction
        ) {
          Text(
              text = actionButtonText.uppercase(),
              style = MaterialTheme.typography.button,
          )
        }
      }

      Text(
          modifier = Modifier
              .offset(y = (-dimensionResource(R.dimen.spacing_4)))
              .padding(end = dimensionResource(R.dimen.spacing_16)),
          text = appointmentDate,
          style = MaterialTheme.typography.body1,
          color = MaterialTheme.colors.onSurface,
      )

      if (!facilityName.isNullOrEmpty()) {
        Text(
            modifier = Modifier
                .padding(
                    top = dimensionResource(R.dimen.spacing_4),
                    bottom = dimensionResource(R.dimen.spacing_4),
                    end = dimensionResource(R.dimen.spacing_16)
                ),
            text = facilityName,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface,
        )
      }
    }
  }
}

@Preview
@Composable
fun NextAppointmentWithFacilityNamePreview(modifier: Modifier = Modifier) {
  SimpleTheme {
    NextAppointment(
        appointmentDate = "30-Dec-2021 Today",
        facilityName = "UHC Khardi",
        actionButtonText = "Change"
    ) { }
  }
}

@Preview
@Composable
fun NextAppointmentWithoutFacilityNamePreview(modifier: Modifier = Modifier) {
  SimpleTheme {
    NextAppointment(
        appointmentDate = "30-Dec-2021 Today",
        actionButtonText = "Add"
    ) { }
  }
}
