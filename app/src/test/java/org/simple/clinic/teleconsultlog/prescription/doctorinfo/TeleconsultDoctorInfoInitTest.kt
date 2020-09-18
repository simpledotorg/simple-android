package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class TeleconsultDoctorInfoInitTest {

  val model = TeleconsultDoctorInfoModel.create()
  private val initSpec = InitSpec(TeleconsultDoctorInfoInit())

  @Test
  fun `when screen is created, then load initial data`() {
    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadSignatureBitmap, LoadMedicalRegistrationId)
        ))
  }

  @Test
  fun `when screen is restored and medical registration is loaded, then set medical registration id`() {
    val medicalRegistrationId = "1234567890"
    val medicalRegistrationIdModel = model.medicalRegistrationIdLoaded(medicalRegistrationId)

    initSpec
        .whenInit(medicalRegistrationIdModel)
        .then(assertThatFirst(
            hasModel(medicalRegistrationIdModel),
            hasEffects(LoadSignatureBitmap, SetMedicalRegistrationId(medicalRegistrationId))
        ))
  }
}
