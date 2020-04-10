package org.simple.clinic.patientcontact

import android.Manifest
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RequiresPermission
import org.simple.clinic.util.RuntimePermissionResult

sealed class PatientContactEvent

data class PatientProfileLoaded(
    val patientProfile: PatientProfile
) : PatientContactEvent()

data class OverdueAppointmentLoaded(
    val overdueAppointment: Optional<OverdueAppointment>
) : PatientContactEvent()

data class NormalCallClicked(
    override var permission: Optional<RuntimePermissionResult> = None,
    override val permissionRequestCode: Int = 1,
    override val permissionString: String = Manifest.permission.CALL_PHONE
) : PatientContactEvent(), RequiresPermission

data class SecureCallClicked(
    override var permission: Optional<RuntimePermissionResult> = None,
    override val permissionRequestCode: Int = 2,
    override val permissionString: String = Manifest.permission.CALL_PHONE
) : PatientContactEvent(), RequiresPermission
