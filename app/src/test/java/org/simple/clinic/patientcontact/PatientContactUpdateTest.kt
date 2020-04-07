package org.simple.clinic.patientcontact

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class PatientContactUpdateTest {

  @Test
  fun `when the patient profile is loaded, the ui must be updated`() {
    val spec = UpdateSpec(PatientContactUpdate())
    val defaultModel = PatientContactModel.create()
    val patientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("b5eccb67-6425-4d48-9c17-65e9b267f9eb"),
        generatePhoneNumber = true
    )

    spec
        .given(defaultModel)
        .whenEvent(PatientProfileLoaded(patientProfile))
        .then(assertThatNext(
            hasModel(defaultModel.patientProfileLoaded(patientProfile)),
            hasNoEffects()
        ))
  }
}
