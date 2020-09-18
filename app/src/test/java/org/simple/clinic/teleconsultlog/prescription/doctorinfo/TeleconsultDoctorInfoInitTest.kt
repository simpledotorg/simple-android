package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class TeleconsultDoctorInfoInitTest {

  @Test
  fun `when screen is created, then load initial data`() {
    val model = TeleconsultDoctorInfoModel.create()
    InitSpec(TeleconsultDoctorInfoInit())
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadMedicalRegistrationId)
        ))
  }
}
