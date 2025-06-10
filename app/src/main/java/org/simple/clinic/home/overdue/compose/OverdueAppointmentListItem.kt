package org.simple.clinic.home.overdue.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.ButtonSize
import org.simple.clinic.common.ui.components.OutlinedButton
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun OverdueAppointmentListItem(
    uiModels: List<OverdueUiModel>
) {
    SimpleTheme {
        LazyColumn(
            modifier = Modifier.padding(
                start = 8.dp,
                end = 8.dp,
                top = 8.dp,
                bottom = 128.dp
            )
        ) {
            items(uiModels) { model ->
                when (model) {
                    is OverdueUiModel.Patient -> {
                        OverduePatientListItem(
                            modifier = Modifier.padding(bottom = 8.dp),
                            appointmentUuid = model.appointmentUuid,
                            patientUuid = model.patientUuid,
                            name = model.name,
                            gender = model.gender,
                            age = model.age,
                            phoneNumber = model.phoneNumber,
                            overdueDays = model.overdueDays,
                            villageName = model.villageName,
                            isOverdueSelectAndDownloadEnabled = model.isOverdueSelectAndDownloadEnabled,
                            isAppointmentSelected = model.isAppointmentSelected,
                            isEligibleForReassignment = model.isEligibleForReassignment,
                            onCallClicked = model.onCallClicked,
                            onRowClicked = model.onRowClicked,
                            onCheckboxClicked = model.onCheckboxClicked
                        )
                    }

                    is OverdueUiModel.Header -> {
                        OverdueSectionHeader(
                            headerTextRes = model.headerTextRes,
                            count = model.count,
                            isExpanded = model.isOverdueSectionHeaderExpanded,
                            overdueAppointmentSectionTitle = model.overdueAppointmentSectionTitle,
                            locale = model.locale,
                            onClick = model.onToggle
                        )
                    }

                    is OverdueUiModel.Footer -> {
                        OverdueSectionFooter(
                            pendingListState = model.pendingListState,
                            onClick = model.onClick
                        )
                    }

                    is OverdueUiModel.Divider -> {
                        Divider(
                            color = colorResource(R.color.color_on_surface_11),
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    is OverdueUiModel.NoPendingPatients -> {
                        NoPendingPatients()
                    }

                    is OverdueUiModel.SearchButton -> {
                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth(),
                            buttonSize = ButtonSize.Big,
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search Overdue Patient"
                                )
                            },
                            onClick = model.onSearchClicked,
                        ) {
                            ProvideTextStyle(value = SimpleTheme.typography.body0) {
                                Text(text = stringResource(id = R.string.overdue_search_patient_name_or_village))
                            }
                        }
                    }
                }
            }
        }
    }
}
