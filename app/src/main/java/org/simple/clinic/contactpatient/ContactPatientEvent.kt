package org.simple.clinic.contactpatient

import android.Manifest
import org.simple.clinic.activity.permissions.RequiresPermission
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate
import java.util.Optional

sealed class ContactPatientEvent : UiEvent

data class PatientProfileLoaded(
    val patientProfile: ContactPatientProfile
) : ContactPatientEvent()

data class OverdueAppointmentLoaded(
    val overdueAppointment: Optional<Appointment>
) : ContactPatientEvent()

data class CurrentFacilityLoaded(
    val currentFacility: Facility
) : ContactPatientEvent()

data class NormalCallClicked(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionRequestCode: Int = 1,
    override val permissionString: String = Manifest.permission.CALL_PHONE
) : ContactPatientEvent(), RequiresPermission {

  override val analyticsName: String = "Contact Patient:Normal Call Clicked"
}

data class SecureCallClicked(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionRequestCode: Int = 2,
    override val permissionString: String = Manifest.permission.CALL_PHONE
) : ContactPatientEvent(), RequiresPermission {

  override val analyticsName: String = "Contact Patient:Secure Call Clicked"
}

object PatientMarkedAsAgreedToVisit : ContactPatientEvent()

object PatientAgreedToVisitClicked : ContactPatientEvent() {
  override val analyticsName: String = "Contact Patient:Agreed to visit clicked"
}

object NextReminderDateClicked : ContactPatientEvent() {
  override val analyticsName: String = "Contact Patient:Next appointment date clicked"
}

object PreviousReminderDateClicked : ContactPatientEvent() {
  override val analyticsName: String = "Contact Patient:Previous appointment date clicked"
}

data class ManualDateSelected(
    val selectedDate: LocalDate,
    val currentDate: LocalDate
) : ContactPatientEvent() {
  override val analyticsName: String = "Contact Patient:Manual date selected"
}

object AppointmentDateClicked : ContactPatientEvent() {
  override val analyticsName: String = "Contact Patient:Appointment date clicked"
}

object ReminderSetForAppointment : ContactPatientEvent()

object SaveAppointmentReminderClicked : ContactPatientEvent() {
  override val analyticsName: String = "Contact Patient:Save appointment reminder clicked"
}

object RemindToCallLaterClicked : ContactPatientEvent() {
  override val analyticsName: String = "Contact Patient:Remind to call later clicked"
}

object BackClicked : ContactPatientEvent()

object RemoveFromOverdueListClicked : ContactPatientEvent()

data class CallResultForAppointmentLoaded(val callResult: Optional<CallResult>) : ContactPatientEvent()
