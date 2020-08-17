package org.simple.clinic.teleconsultlog.success

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.GoToHomeScreen
import java.util.UUID

class TeleConsultSuccessUpdateTest {

  private val updateSpec = UpdateSpec(TeleConsultSuccessUpdate())
  private val patientUuid = UUID.fromString("f2f02504-5d02-4291-a15b-7de095ceebe2")
  private val defaultModel = TeleConsultSuccessModel.create(patientUuid)

  @Test
  fun `when the patient detail is loaded, then update the UI`() {
    val patient = TestData.patient(patientUuid)
    updateSpec
        .given(defaultModel)
        .whenEvents(PatientDetailsLoaded(patient))
        .then  (
            assertThatNext(
                hasModel(defaultModel.patientDetailLoaded(patient)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when No button is clicked, then go back to home screen`(){
    updateSpec
        .given(defaultModel)
        .whenEvents(NoPrescriptionClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(GoToHomeScreen as TeleConsultSuccessEffect)
            )
        )
  }
}
