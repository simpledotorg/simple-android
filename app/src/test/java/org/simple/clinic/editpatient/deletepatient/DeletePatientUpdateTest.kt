package org.simple.clinic.editpatient.deletepatient

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
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
  fun `when patient name is loaded, then update ui`() {
    val patientName = "John Doe"
    val patient = TestData.patient(
        uuid = UUID.fromString("c2d4dbb0-a045-4461-b3e6-890e0ba9fc6b"),
        fullName = patientName
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientLoaded(patient))
        .then(assertThatNext(
            hasModel(defaultModel.patientNameLoaded(patientName)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when patient is being deleted, then the delete patient confirmation alert should be shown`() {
    val model = defaultModel.patientNameLoaded(patientName)
    val patientDeleteReason = PatientDeleteReason.Duplicate

    updateSpec
        .given(model)
        .whenEvent(PatientDeleteReasonClicked(patientDeleteReason))
        .then(assertThatNext(
            hasModel(model.deleteReasonSelected(patientDeleteReason)),
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
            hasModel(model.deleteReasonSelected(patientDeleteReason)),
            hasEffects(ShowConfirmDiedDialog(patientName = patientName) as DeletePatientEffect)
        ))
  }

  @Test
  fun `when patient is deleted, then show the home screen`() {
    val model = defaultModel.patientNameLoaded(patientName)

    updateSpec
        .given(model)
        .whenEvent(PatientDeleted)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen as DeletePatientEffect)
        ))
  }

  @Test
  fun `when patient is marked as dead, then show the home screen`() {
    val model = defaultModel.patientNameLoaded(patientName)

    updateSpec
        .given(model)
        .whenEvent(PatientMarkedAsDead)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen as DeletePatientEffect)
        ))
  }

  @Test
  fun `when confirm patient delete is clicked, then delete the patient`() {
    val model = defaultModel.patientNameLoaded(patientName)
    val deletedReason = DeletedReason.AccidentalRegistration

    updateSpec
        .given(model)
        .whenEvent(ConfirmPatientDeleteClicked(deletedReason))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(DeletePatient(patientUuid, deletedReason) as DeletePatientEffect)
        ))
  }

  @Test
  fun `when confirm patient died is clicked, then mark the patient as dead`() {
    val model = defaultModel.patientNameLoaded(patientName)

    updateSpec
        .given(model)
        .whenEvent(ConfirmPatientDiedClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkPatientAsDead(patientUuid) as DeletePatientEffect)
        ))
  }
}
