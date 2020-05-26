package org.simple.clinic.editpatient.deletepatient

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.DeletedReason
import java.util.UUID

class DeletePatientUpdateTest {

  private val updateSpec = UpdateSpec(DeletePatientUpdate())
  private val patientUuid = UUID.fromString("27c9456d-c8a6-4de9-bdd4-f249dff59de1")
  private val patientName = "John Doe"
  private val defaultModel = DeletePatientModel.default(
      patientUuid = patientUuid
  )

  @Test
  fun `when patient is being deleted, then the delete patient confirmation alert should be shown`() {
    val model = defaultModel.patientNameLoaded(patientName)
    val patientDeleteReason = PatientDeleteReason.Duplicate

    updateSpec
        .given(model)
        .whenEvent(PatientDeleteReasonClicked(patientDeleteReason))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowConfirmDeleteDialog(patientName = patientName, deletedReason = DeletedReason.Duplicate) as DeletePatientEffect)
        ))
  }

  @Test
  fun `when patient is being deleted because they have died, then patient died confirmation alert should be shown`() {
    val model = defaultModel.patientNameLoaded(patientName)
    val patientDeleteReason = PatientDeleteReason.Died

    updateSpec
        .given(model)
        .whenEvent(PatientDeleteReasonClicked(patientDeleteReason))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowConfirmDiedDialog(patientName = patientName) as DeletePatientEffect)
        ))
  }
}
