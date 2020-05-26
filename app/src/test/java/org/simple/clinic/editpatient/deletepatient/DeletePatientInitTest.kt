package org.simple.clinic.editpatient.deletepatient

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class DeletePatientInitTest {

  private val initSpec = InitSpec(DeletePatientInit())
  private val patientUuid = UUID.fromString("105cb76d-502f-4765-b3ec-21464005b69e")
  private val defaultModel = DeletePatientModel.default(patientUuid)

  @Test
  fun `when screen is created, then load patient`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadPatient(patientUuid) as DeletePatientEffect)
        ))
  }

  @Test
  fun `when screen is restored and patient name is already loaded, then don't load patient again`() {
    val patientName = "John Doe"
    val model = defaultModel.patientNameLoaded(patientName)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasNoEffects()
        ))
  }
}
