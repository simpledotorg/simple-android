package org.simple.clinic.patientcontact

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.util.Optional
import org.simple.clinic.util.ParcelableOptional
import org.simple.clinic.util.parcelable
import java.util.UUID

@Parcelize
data class PatientContactModel(
    val patientUuid: UUID,
    val patientProfile: PatientProfile? = null,
    val appointment: ParcelableOptional<OverdueAppointment>? = null,
    val secureCallingFeatureEnabled: Boolean
) : Parcelable {

  companion object {
    fun create(
        patientUuid: UUID,
        phoneNumberMaskerConfig: PhoneNumberMaskerConfig
    ): PatientContactModel {
      val secureCallingFeatureEnabled = with(phoneNumberMaskerConfig) {
        phoneMaskingFeatureEnabled && proxyPhoneNumber.isNotBlank()
      }

      return PatientContactModel(
          patientUuid = patientUuid,
          secureCallingFeatureEnabled = secureCallingFeatureEnabled
      )
    }
  }

  val hasLoadedPatientProfile: Boolean
    get() = patientProfile != null

  val hasLoadedAppointment: Boolean
    get() = appointment != null

  fun patientProfileLoaded(patientProfile: PatientProfile): PatientContactModel {
    return copy(patientProfile = patientProfile)
  }

  fun overdueAppointmentLoaded(appointment: Optional<OverdueAppointment>): PatientContactModel {
    return copy(appointment = appointment.parcelable())
  }
}
