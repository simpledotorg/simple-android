package org.simple.clinic.teleconsultlog.success

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
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
}
