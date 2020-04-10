package org.simple.clinic.patientcontact

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.util.Just
import java.util.UUID

class PatientContactUpdateTest {

  private val patientUuid = UUID.fromString("b5eccb67-6425-4d48-9c17-65e9b267f9eb")
  private val patientProfile = TestData.patientProfile(
      patientUuid = patientUuid,
      generatePhoneNumber = true
  )
  private val overdueAppointment = TestData.overdueAppointment(
      patientUuid = patientUuid,
      facilityUuid = UUID.fromString("c97a8b30-8094-4c93-9ad6-ecc100130943"),
      phoneNumber = patientProfile.phoneNumbers.first(),
      appointmentUuid = UUID.fromString("f1b11fa6-3622-4f82-b74b-dd08dd563f1a"),
      gender = patientProfile.patient.gender,
      age = patientProfile.patient.age,
      dateOfBirth = patientProfile.patient.dateOfBirth
  )
  private val proxyPhoneNumberForSecureCalls = "9999988888"

  private val spec = UpdateSpec(PatientContactUpdate(proxyPhoneNumberForMaskedCalls = proxyPhoneNumberForSecureCalls))

  @Test
  fun `when the patient profile is loaded, the ui must be updated`() {
    val defaultModel = defaultModel()

    spec
        .given(defaultModel)
        .whenEvent(PatientProfileLoaded(patientProfile))
        .then(assertThatNext(
            hasModel(defaultModel.patientProfileLoaded(patientProfile)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the overdue appointment is loaded, the ui must be updated`() {
    val appointment = Just(overdueAppointment)
    val defaultModel = defaultModel()

    spec
        .given(defaultModel)
        .whenEvent(OverdueAppointmentLoaded(appointment))
        .then(assertThatNext(
            hasModel(defaultModel.overdueAppointmentLoaded(appointment))
        ))
  }

  private fun defaultModel(
      phoneMaskFeatureEnabled: Boolean = false,
      proxyPhoneNumber: String = proxyPhoneNumberForSecureCalls
  ): PatientContactModel {
    val phoneNumberMaskerConfig = PhoneNumberMaskerConfig(proxyPhoneNumber, phoneMaskFeatureEnabled)

    return PatientContactModel.create(patientUuid, phoneNumberMaskerConfig)
  }
}
