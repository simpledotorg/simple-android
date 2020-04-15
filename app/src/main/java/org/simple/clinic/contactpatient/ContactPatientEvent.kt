package org.simple.clinic.contactpatient

import android.Manifest
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RequiresPermission
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent

sealed class ContactPatientEvent : UiEvent

data class PatientProfileLoaded(
    val patientProfile: PatientProfile
) : ContactPatientEvent()

data class OverdueAppointmentLoaded(
    val overdueAppointment: Optional<OverdueAppointment>
) : ContactPatientEvent()

data class NormalCallClicked(
    override var permission: Optional<RuntimePermissionResult> = None,
    override val permissionRequestCode: Int = 1,
    override val permissionString: String = Manifest.permission.CALL_PHONE
) : ContactPatientEvent(), RequiresPermission {

  override val analyticsName: String = "Contact Patient:Normal Call Clicked"
}

data class SecureCallClicked(
    override var permission: Optional<RuntimePermissionResult> = None,
    override val permissionRequestCode: Int = 2,
    override val permissionString: String = Manifest.permission.CALL_PHONE
) : ContactPatientEvent(), RequiresPermission {

  override val analyticsName: String = "Contact Patient:Secure Call Clicked"
}

object PatientMarkedAsAgreedToVisit : ContactPatientEvent()

object PatientAgreedToVisitClicked : ContactPatientEvent() {
  override val analyticsName: String = "Contact Patient:Agreed to visit clicked"
}

object NextReminderDateClicked: ContactPatientEvent() {
  override val analyticsName: String
    get() = "Contact Patient:Next appointment date clicked"
}
