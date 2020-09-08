package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class TeleconsultRecordUpdate : Update<TeleconsultRecordModel, TeleconsultRecordEvent, TeleconsultRecordEffect> {

  override fun update(model: TeleconsultRecordModel, event: TeleconsultRecordEvent): Next<TeleconsultRecordModel, TeleconsultRecordEffect> {
    return when (event) {
      BackClicked -> dispatch(GoBack)
      is TeleconsultRecordWithPrescribedDrugsLoaded -> teleconsultRecordWithPrescribedDrugsLoaded(model, event)
      TeleconsultRecordCreated -> dispatch(NavigateToTeleconsultSuccess)
      is DoneClicked -> dispatch(CreateTeleconsultRecord(
          patientUuid = model.patientUuid,
          teleconsultRecordId = model.teleconsultRecordId,
          teleconsultationType = event.teleconsultationType,
          patientTookMedicine = event.patientTookMedicines,
          patientConsented = event.patientConsented
      ))
      is PatientDetailsLoaded -> noChange()
    }
  }

  private fun teleconsultRecordWithPrescribedDrugsLoaded(
      model: TeleconsultRecordModel,
      event: TeleconsultRecordWithPrescribedDrugsLoaded
  ): Next<TeleconsultRecordModel, TeleconsultRecordEffect> {
    val teleconsultRecordInfo = event.teleconsultRecordWithPrescribedDrugs?.teleconsultRecord?.teleconsultRecordInfo
    return if (teleconsultRecordInfo != null) {
      next(model.teleconsultRecordLoaded(teleconsultRecordInfo))
    } else {
      noChange()
    }
  }
}
