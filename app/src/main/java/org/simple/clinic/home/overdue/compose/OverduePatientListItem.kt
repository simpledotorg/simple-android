package org.simple.clinic.home.overdue.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayIconRes
import java.util.UUID

@Composable
fun OverduePatientListItem(
    modifier: Modifier = Modifier,
    appointmentUuid: UUID,
    patientUuid: UUID,
    name: String,
    gender: Gender,
    age: Int,
    phoneNumber: String?,
    overdueDays: Int,
    villageName: String?,
    isOverdueSelectAndDownloadEnabled: Boolean,
    isAppointmentSelected: Boolean,
    isEligibleForReassignment: Boolean,
    onCallClicked: (UUID) -> Unit,
    onRowClicked: (UUID) -> Unit,
    onCheckboxClicked: (UUID) -> Unit
) {
  val overdueText = pluralStringResource(
      id = R.plurals.overdue_list_item_appointment_overdue_days, count = overdueDays, overdueDays
  )

  Card(
      modifier = modifier
          .fillMaxWidth()
          .clickable { onRowClicked(patientUuid) },
  ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = dimensionResource(R.dimen.spacing_16))
    ) {
      Row(
          verticalAlignment = Alignment.Top,
          modifier = Modifier
              .fillMaxWidth()
      ) {
        OverduePatientListLeftIcon(
            isOverdueSelectAndDownloadEnabled = isOverdueSelectAndDownloadEnabled,
            isAppointmentSelected = isAppointmentSelected,
            gender = gender,
            appointmentUuid = appointmentUuid,
            onCheckboxClicked = onCheckboxClicked
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = dimensionResource(R.dimen.spacing_12)),
        ) {
          Text(
              text = stringResource(R.string.overdue_list_item_name_age, name, age),
              style = SimpleTheme.typography.body0Medium,
              color = SimpleTheme.colors.material.primary
          )

          EligibleForReassignmentView(isEligibleForReassignment)

          PatientVillageView(villageName)

          Text(
              modifier = Modifier
                  .padding(top = dimensionResource(R.dimen.spacing_12)),
              text = overdueText,
              style = SimpleTheme.typography.material.body2,
              color = SimpleTheme.colors.material.error,
          )
        }

        OverduePatientListItemRightButton(
            modifier = Modifier.align(alignment = Alignment.CenterVertically),
            patientUuid = patientUuid,
            phoneNumber = phoneNumber,
            onCallClicked = onCallClicked
        )
      }
    }
  }
}

@Composable
private fun EligibleForReassignmentView(
    isEligibleForReassignment: Boolean
) {
  if (isEligibleForReassignment) {
    Row(
        modifier = Modifier
            .padding(top = dimensionResource(R.dimen.spacing_4)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
          modifier = Modifier.size(dimensionResource(R.dimen.spacing_16)),
          painter = painterResource(id = R.drawable.ic_facility_reassignment),
          contentDescription = null,
          tint = Color.Unspecified,
      )
      Text(
          modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_4)),
          text = stringResource(R.string.patient_facility_reassignment),
          style = SimpleTheme.typography.material.body2,
          color = colorResource(id = org.simple.clinic.common.R.color.simple_green_500)
      )
    }
  }
}

@Composable
private fun PatientVillageView(
    villageName: String?
) {
  if (!villageName.isNullOrBlank()) {
    Row(
        modifier = Modifier
            .padding(top = dimensionResource(R.dimen.spacing_12)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          modifier = Modifier.padding(end = dimensionResource(R.dimen.spacing_8)),
          text = stringResource(R.string.overdue_list_item_village),
          style = SimpleTheme.typography.body2Bold,
          color = SimpleTheme.colors.material.onSurface.copy(alpha = 0.67f)
      )
      Text(
          text = villageName,
          style = SimpleTheme.typography.material.body2,
          color = SimpleTheme.colors.material.onSurface.copy(alpha = 0.67f)
      )
    }
  }
}

@Composable
fun OverduePatientListLeftIcon(
    isOverdueSelectAndDownloadEnabled: Boolean,
    isAppointmentSelected: Boolean,
    appointmentUuid: UUID,
    gender: Gender,
    onCheckboxClicked: (UUID) -> Unit,
) {
  if (isOverdueSelectAndDownloadEnabled) {
    Checkbox(
        modifier = Modifier.size(24.dp),
        checked = isAppointmentSelected,
        colors = CheckboxDefaults.colors(
            checkedColor = SimpleTheme.colors.material.primary,
            uncheckedColor = SimpleTheme.colors.material.onSurface.copy(alpha = 0.67f),
        ),
        onCheckedChange = { onCheckboxClicked(appointmentUuid) },
    )
  } else {
    Image(
        painter = painterResource(id = gender.displayIconRes),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
    )
  }
}

@Composable
fun OverduePatientListItemRightButton(
    modifier: Modifier,
    patientUuid: UUID,
    phoneNumber: String?,
    onCallClicked: (UUID) -> Unit
) {
  IconButton(
      onClick = { onCallClicked(patientUuid) },
      modifier = modifier
          .size(40.dp)
  ) {
    Image(
        painter = painterResource(
            id = if (phoneNumber.isNullOrBlank()) R.drawable.ic_overdue_no_phone_number
            else R.drawable.ic_overdue_call
        ),
        contentDescription = null,
    )
  }
}

@Preview
@Composable
private fun OverduePatientListItemPreview() {
  SimpleTheme {
    OverduePatientListItem(
        appointmentUuid = UUID.fromString("770895ad-0db8-42bf-ba3e-df78bf1b4706"),
        patientUuid = UUID.fromString("c6c6b987-86c9-4334-aa30-06ca0f02a70e"),
        name = "Ali",
        gender = Gender.Male,
        age = 44,
        phoneNumber = "9876543210",
        overdueDays = 43,
        villageName = "New Village",
        isOverdueSelectAndDownloadEnabled = false,
        isAppointmentSelected = false,
        isEligibleForReassignment = true,
        onCallClicked = {},
        onRowClicked = {},
        onCheckboxClicked = {}
    )
  }
}

