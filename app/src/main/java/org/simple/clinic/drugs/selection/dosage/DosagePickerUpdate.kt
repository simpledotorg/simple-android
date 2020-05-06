package org.simple.clinic.drugs.selection.dosage

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class DosagePickerUpdate : Update<DosagePickerModel, DosagePickerEvent, DosagePickerEffect> {

  override fun update(model: DosagePickerModel, event: DosagePickerEvent): Next<DosagePickerModel, DosagePickerEffect> {
    return when (event) {
      is DrugsLoaded -> next(model.protocolDrugsLoaded(event.protocolDrugs))
      NoneSelected -> {
        val effect = if (model.hasExistingPrescription) DeleteExistingPrescription(model.existingPrescriptionUuid!!) else CloseScreen

        dispatch(effect)
      }
      ExistingPrescriptionDeleted, NewPrescriptionCreated, ExistingPrescriptionChanged -> dispatch(CloseScreen)
      is DosageSelected -> {
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

        dispatch(effect)
      }
    }
  }
}
