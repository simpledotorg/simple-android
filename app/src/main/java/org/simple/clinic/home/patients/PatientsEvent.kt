package org.simple.clinic.home.patients

import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant

sealed class PatientsEvent : UiEvent

class PatientsEnterCodeManuallyClicked : PatientsEvent() {
  override val analyticsName = "Patients:Enter Code Manually Clicked"
}

object NewPatientClicked : PatientsEvent() {
  override val analyticsName = "Patients:Search For Patient Clicked"
}

data class UserDetailsLoaded(val user: User): PatientsEvent()

object ActivityResumed: PatientsEvent()

data class DataForShowingApprovedStatusLoaded(
    val currentTime: Instant,
    val approvalStatusUpdatedAt: Instant,
    val hasBeenDismissed: Boolean
): PatientsEvent()

class UserApprovedStatusDismissed : PatientsEvent() {
  override val analyticsName = "Patients:Dismissed User Approved Status"
}
