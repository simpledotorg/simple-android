package org.simple.clinic.home.overdue.compose

import androidx.annotation.StringRes
import org.simple.clinic.home.overdue.OverdueAppointmentSectionTitle
import org.simple.clinic.home.overdue.PendingListState
import org.simple.clinic.patient.Gender
import java.util.Locale
import java.util.UUID

sealed class OverdueUiModel {
  data class Patient(
      val appointmentUuid: UUID,
      val patientUuid: UUID,
      val name: String,
      val gender: Gender,
      val age: Int,
      val phoneNumber: String? = null,
      val overdueDays: Int,
      val villageName: String?,
      val isOverdueSelectAndDownloadEnabled: Boolean,
      val isAppointmentSelected: Boolean,
      val isEligibleForReassignment: Boolean,
      val onCallClicked: (UUID) -> Unit,
      val onRowClicked: (UUID) -> Unit,
      val onCheckboxClicked: (UUID) -> Unit
  ) : OverdueUiModel()

  data class Header(
      @StringRes val headerTextRes: Int,
      val count: Int,
      val isOverdueSectionHeaderExpanded: Boolean,
      val overdueAppointmentSectionTitle: OverdueAppointmentSectionTitle,
      val locale: Locale,
      val onToggle: (OverdueAppointmentSectionTitle) -> Unit,
  ) : OverdueUiModel()

  data class Footer(
      val pendingListState: PendingListState,
      val onClick: () -> Unit
  ) : OverdueUiModel()

  data object Divider : OverdueUiModel()

  data object NoPendingPatients : OverdueUiModel()

  data class SearchButton(
      val onSearchClicked: () -> Unit
  ) : OverdueUiModel()
}
