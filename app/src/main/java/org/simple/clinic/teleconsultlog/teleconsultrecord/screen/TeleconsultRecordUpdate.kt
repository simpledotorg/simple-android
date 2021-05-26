package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class TeleconsultRecordUpdate : Update<TeleconsultRecordModel, TeleconsultRecordEvent, TeleconsultRecordEffect> {

  override fun update(
      model: TeleconsultRecordModel,
      event: TeleconsultRecordEvent
  ): Next<TeleconsultRecordModel, TeleconsultRecordEffect> {
    return when (event) {
      BackClicked -> dispatch(ValidateTeleconsultRecord(model.teleconsultRecordId))
      is TeleconsultRecordLoaded -> teleconsultRecordLoaded(model, event)
      is TeleconsultRecordCreated -> dispatch(ClonePatientPrescriptions(model.patientUuid, model.teleconsultRecordId))
      is DoneClicked -> next(
          model.creatingTeleconsultRecord(),
          CreateTeleconsultRecord(
              patientUuid = model.patientUuid,
              teleconsultRecordId = model.teleconsultRecordId,
              teleconsultationType = event.teleconsultationType,
              patientTookMedicine = event.patientTookMedicines,
              patientConsented = event.patientConsented
          ))
      is PatientDetailsLoaded -> next(model.patientLoaded(event.patient))
      is TeleconsultRecordValidated -> teleconsultRecordValidated(event)
      PatientPrescriptionsCloned -> next(
          model.teleconsultRecordCreated(),
          NavigateToTeleconsultSuccess
      )
    }
  }

  private fun teleconsultRecordValidated(event: TeleconsultRecordValidated): Next<TeleconsultRecordModel, TeleconsultRecordEffect> {
    return if (event.teleconsultRecordExists) {
      dispatch(GoBack)
    } else {
      dispatch(ShowTeleconsultNotRecordedWarning)
    }
  }

  private fun teleconsultRecordLoaded(
      model: TeleconsultRecordModel,
      event: TeleconsultRecordLoaded
  ): Next<TeleconsultRecordModel, TeleconsultRecordEffect> {
    val teleconsultRecordInfo = event.teleconsultRecord?.teleconsultRecordInfo
    return if (teleconsultRecordInfo != null) {
      next(model.teleconsultRecordLoaded(teleconsultRecordInfo))
    } else {
      noChange()
    }
  }
}
