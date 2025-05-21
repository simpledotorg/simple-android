package org.simple.clinic.home.overdue.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                .padding(bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                if (isOverdueSelectAndDownloadEnabled) {
                    Checkbox(
                        modifier = Modifier.size(24.dp),
                        checked = isAppointmentSelected,
                        onCheckedChange = { onCheckboxClicked(appointmentUuid) },
                    )
                } else {
                    Icon(
                        painter = painterResource(id = gender.displayIconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.overdue_list_item_name_age, name, age),
                        style = SimpleTheme.typography.body0Medium,
                        color = SimpleTheme.colors.material.primary
                    )

                    if (isEligibleForReassignment) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(id = R.drawable.ic_facility_reassignment),
                                contentDescription = null,
                                tint = SimpleTheme.colors.material.primary,
                            )
                            Text(
                                modifier = Modifier.padding(start = 4.dp),
                                text = stringResource(R.string.patient_facility_reassignment),
                                style = SimpleTheme.typography.body2Numeric,
                                color = SimpleTheme.colors.material.primary
                            )
                        }
                    }

                    if (!villageName.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text(
                                modifier = Modifier.padding(end = 8.dp),
                                text = stringResource(R.string.overdue_list_item_village),
                                color = SimpleTheme.colors.material.onSurface.copy(alpha = 0.67f)
                            )
                            Text(
                                text = villageName,
                            )
                        }
                    }

                    Text(
                        text = overdueText,
                        color = SimpleTheme.colors.material.error,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                IconButton(
                    onClick = { onCallClicked(patientUuid) },
                    modifier = Modifier
                        .size(40.dp)
                        .align(alignment = Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (phoneNumber.isNullOrBlank()) R.drawable.ic_overdue_no_phone_number
                            else R.drawable.ic_overdue_call
                        ),
                        contentDescription = null,
                        tint = SimpleTheme.colors.material.primary
                    )
                }
            }
        }
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

