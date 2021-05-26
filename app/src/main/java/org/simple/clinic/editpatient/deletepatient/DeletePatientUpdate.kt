package org.simple.clinic.editpatient.deletepatient

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.patient.DeletedReason

class DeletePatientUpdate : Update<DeletePatientModel, DeletePatientEvent, DeletePatientEffect> {
  override fun update(
      model: DeletePatientModel,
      event: DeletePatientEvent
  ): Next<DeletePatientModel, DeletePatientEffect> {
    return when (event) {
      is PatientDeleteReasonClicked -> patientDeleteReasonClicked(model, event)
      is PatientLoaded -> patientNameLoaded(model, event)
      PatientDeleted, PatientMarkedAsDead -> dispatch(ShowHomeScreen)
      is ConfirmPatientDeleteClicked -> dispatch(DeletePatient(model.patientUuid, event.deletedReason))
      is ConfirmPatientDiedClicked -> dispatch(MarkPatientAsDead(model.patientUuid))
    }
  }

  private fun patientNameLoaded(
      model: DeletePatientModel,
      event: PatientLoaded
  ): Next<DeletePatientModel, DeletePatientEffect> =
      next(model.patientNameLoaded(event.patient.fullName))

  private fun patientDeleteReasonClicked(
      model: DeletePatientModel,
      event: PatientDeleteReasonClicked
  ): Next<DeletePatientModel, DeletePatientEffect> {
    val effect = when (event.patientDeleteReason) {
      PatientDeleteReason.Duplicate -> ShowConfirmDeleteDialog(model.patientName!!, DeletedReason.Duplicate)
      PatientDeleteReason.AccidentalRegistration -> ShowConfirmDeleteDialog(model.patientName!!, DeletedReason.AccidentalRegistration)
      PatientDeleteReason.Died -> ShowConfirmDiedDialog(model.patientName!!)
    }

    return next(model.deleteReasonSelected(event.patientDeleteReason), effect)
  }
}
