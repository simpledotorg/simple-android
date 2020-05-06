package org.simple.clinic.drugs.selection.dosage

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class DosagePickerUpdate : Update<DosagePickerModel, DosagePickerEvent, DosagePickerEffect> {

  override fun update(model: DosagePickerModel, event: DosagePickerEvent): Next<DosagePickerModel, DosagePickerEffect> {
    return when (event) {
      is DrugsLoaded -> next(model.protocolDrugsLoaded(event.protocolDrugs))
      is NoneSelected -> {
        val effect = if (model.hasExistingPrescription) DeleteExistingPrescription(model.existingPrescriptionUuid!!) else CloseScreen

        dispatch(effect)
      }
      is ExistingPrescriptionDeleted -> dispatch(CloseScreen)
    }
  }
}
