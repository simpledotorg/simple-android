package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class TeleconsultDoctorInfoUpdateTest {

  @Test
  fun `when medical registration id is loaded, then set medical registration id and update model`() {
    val medicalRegistrationId = "1234567890"
    val model = TeleconsultDoctorInfoModel.create()

    UpdateSpec(TeleconsultDoctorInfoUpdate())
        .given(model)
        .whenEvent(MedicalRegistrationIdLoaded(medicalRegistrationId))
        .then(assertThatNext(
            hasModel(model.medicalRegistrationIdLoaded(medicalRegistrationId)),
            hasEffects(SetMedicalRegistrationId(medicalRegistrationId))
        ))
  }
}
