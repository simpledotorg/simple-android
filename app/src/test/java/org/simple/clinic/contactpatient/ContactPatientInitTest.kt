package org.simple.clinic.contactpatient

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.util.None
import java.util.UUID

class ContactPatientInitTest {

  private val patientUuid = UUID.fromString("34556bef-6221-4ffb-a5b7-4e7f30d584c1")
  private val spec = InitSpec(ContactPatientInit())

  @Test
  fun `when the screen is created, load the patient profile and the latest appointment`() {
    val defaultModel = defaultModel()

    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadPatientProfile(patientUuid), LoadLatestOverdueAppointment(patientUuid))
        ))
  }

  @Test
  fun `when the screen is restored, do not load the patient profile and latest appointment if they are already loaded`() {
    val model = defaultModel()
        .patientProfileLoaded(TestData.patientProfile(patientUuid = patientUuid))
        .overdueAppointmentLoaded(None)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasNoEffects()
        ))
  }

  private fun defaultModel(
      phoneMaskFeatureEnabled: Boolean = false,
      proxyPhoneNumber: String = "12345678"
  ): ContactPatientModel {
    val phoneNumberMaskerConfig = PhoneNumberMaskerConfig(proxyPhoneNumber, phoneMaskFeatureEnabled)

    return ContactPatientModel.create(patientUuid, phoneNumberMaskerConfig)
  }
}

