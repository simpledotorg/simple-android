package org.simple.clinic.drugs.selection.dosage

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class DosagePickerUpdate : Update<DosagePickerModel, DosagePickerEvent, DosagePickerEffect> {

  override fun update(
      model: DosagePickerModel,
      event: DosagePickerEvent
  ): Next<DosagePickerModel, DosagePickerEffect> {
    return when (event) {
      is DrugsLoaded -> next(model.protocolDrugsLoaded(event.protocolDrugs))
      NoneSelected -> noneSelected(model)
      ExistingPrescriptionDeleted, NewPrescriptionCreated, ExistingPrescriptionChanged -> dispatch(CloseScreen)
      is DosageSelected -> protocolDosageSelected(model, event)
    }
  }

  private fun noneSelected(model: DosagePickerModel): Next<DosagePickerModel, DosagePickerEffect> {
    val effect = if (model.hasExistingPrescription) DeleteExistingPrescription(model.existingPrescriptionUuid!!) else CloseScreen

    return dispatch(effect)
  }

  private fun protocolDosageSelected(
      model: DosagePickerModel,
      event: DosageSelected
  ): Next<DosagePickerModel, DosagePickerEffect> {
    val effect = if (model.hasExistingPrescription) {
      ChangeExistingPrescription(
          patientUuid = model.patientUuid,
          prescriptionUuid = model.existingPrescriptionUuid!!,
          protocolDrug = event.protocolDrug
      )
    } else {
      CreateNewPrescription(
          patientUuid = model.patientUuid,
          protocolDrug = event.protocolDrug
      )
    }

    return dispatch(effect)
  }
}
