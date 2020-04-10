package org.simple.clinic.patientcontact

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.util.Just
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import java.util.UUID

class PatientContactUpdateTest {

  private val patientUuid = UUID.fromString("b5eccb67-6425-4d48-9c17-65e9b267f9eb")
  private val patientPhoneNumber = "1234567890"
  private val patientProfile = TestData.patientProfile(
      patientUuid = patientUuid,
      patientPhoneNumber = patientPhoneNumber
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

  @Test
  fun `when normal call is selected and the call permission is granted, directly call the patient with auto dial`() {
    val model = defaultModel()
        .patientProfileLoaded(patientProfile)
        .overdueAppointmentLoaded(Just(overdueAppointment))

    spec
        .given(model)
        .whenEvent(NormalCallClicked(permission = Just(GRANTED)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(DirectCallWithAutomaticDialer(patientPhoneNumber) as PatientContactEffect)
        ))
  }

  @Test
  fun `when normal call is selected and the call permission is denied, directly call the patient with manual dial`() {
    val model = defaultModel()
        .patientProfileLoaded(patientProfile)
        .overdueAppointmentLoaded(Just(overdueAppointment))

    spec
        .given(model)
        .whenEvent(NormalCallClicked(permission = Just(DENIED)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(DirectCallWithManualDialer(patientPhoneNumber) as PatientContactEffect)
        ))
  }

  @Test
  fun `when secure call is selected and the call permission is granted, masked call the patient with auto dial`() {
    val model = defaultModel(phoneMaskFeatureEnabled = true)
        .patientProfileLoaded(patientProfile)
        .overdueAppointmentLoaded(Just(overdueAppointment))

    spec
        .given(model)
        .whenEvent(SecureCallClicked(permission = Just(GRANTED)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MaskedCallWithAutomaticDialer(patientPhoneNumber = patientPhoneNumber, proxyPhoneNumber = proxyPhoneNumberForSecureCalls) as PatientContactEffect)
        ))
  }

  @Test
  fun `when secure call is selected and the call permission is denied, masked call the patient with manual dial`() {
    val model = defaultModel(phoneMaskFeatureEnabled = true)
        .patientProfileLoaded(patientProfile)
        .overdueAppointmentLoaded(Just(overdueAppointment))

    spec
        .given(model)
        .whenEvent(SecureCallClicked(permission = Just(DENIED)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MaskedCallWithManualDialer(patientPhoneNumber = patientPhoneNumber, proxyPhoneNumber = proxyPhoneNumberForSecureCalls) as PatientContactEffect)
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
