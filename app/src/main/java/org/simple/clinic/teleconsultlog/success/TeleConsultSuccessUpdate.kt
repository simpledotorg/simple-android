package org.simple.clinic.teleconsultlog.success

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.GoToHomeScreen
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.GoToPrescriptionScreen

class TeleConsultSuccessUpdate : Update<TeleConsultSuccessModel, TeleConsultSuccessEvent, TeleConsultSuccessEffect> {
  override fun update(
      model: TeleConsultSuccessModel,
      event: TeleConsultSuccessEvent
  ): Next<TeleConsultSuccessModel, TeleConsultSuccessEffect> {
    return when (event) {
      is PatientDetailsLoaded -> next(model.patientDetailLoaded(event.patient))
      is NoPrescriptionClicked -> dispatch(GoToHomeScreen)
      is YesPrescriptionClicked -> yesPrescriptionButtonClicked(model)
    }
  }

  private fun yesPrescriptionButtonClicked(
      model: TeleConsultSuccessModel
  ): Next<TeleConsultSuccessModel, TeleConsultSuccessEffect> {
    return if (model.hasPatient)
      dispatch(GoToPrescriptionScreen(model.patientUuid, model.teleconsultRecordId))
    else
      next(model.patientDetailLoaded(null))
  }
}
