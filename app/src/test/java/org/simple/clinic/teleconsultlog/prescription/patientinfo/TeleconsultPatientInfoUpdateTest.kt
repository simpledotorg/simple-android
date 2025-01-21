package org.simple.clinic.teleconsultlog.prescription.patientinfo

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.time.LocalDate
import java.util.UUID

class TeleconsultPatientInfoUpdateTest {

  @Test
  fun `when patient profile are loaded, then update the model`() {
    val patientUuid = UUID.fromString("acc2de73-3dca-4bbb-8f9e-5d7e0140a925")
    val patientProfile = TestData.patientProfile(patientUuid = patientUuid)
    val model = TeleconsultPatientInfoModel.create(
        patientUuid = patientUuid,
        prescriptionDate = LocalDate.parse("2018-01-01")
    )

    UpdateSpec(TeleconsultPatientInfoUpdate())
        .given(model)
        .whenEvent(PatientProfileLoaded(patientProfile))
        .then(assertThatNext(
            hasModel(model.patientProfileLoaded(patientProfile)),
            hasNoEffects()
        ))
  }
}
