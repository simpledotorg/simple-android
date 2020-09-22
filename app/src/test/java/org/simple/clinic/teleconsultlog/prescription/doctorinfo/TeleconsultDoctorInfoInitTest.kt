package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class TeleconsultDoctorInfoInitTest {

  private val model = TeleconsultDoctorInfoModel.create()
  private val initSpec = InitSpec(TeleconsultDoctorInfoInit())

  @Test
  fun `when screen is created, then load initial data`() {
    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadSignatureBitmap, LoadMedicalRegistrationId, LoadCurrentUser)
        ))
  }

  @Test
  fun `when screen is restored and medical registration is loaded, then set medical registration id`() {
    val medicalRegistrationId = "1234567890"
    val user = TestData.loggedInUser(uuid = UUID.fromString("dd9b372d-9bd5-4a1c-a4c3-c7ad15dc8278"))
    val updatedModel = model.medicalRegistrationIdLoaded(medicalRegistrationId)
        .currentUserLoaded(user)

    initSpec
        .whenInit(updatedModel)
        .then(assertThatFirst(
            hasModel(updatedModel),
            hasEffects(LoadSignatureBitmap, SetMedicalRegistrationId(medicalRegistrationId))
        ))
  }
}
