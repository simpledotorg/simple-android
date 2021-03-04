package org.simple.clinic.summary.linkId

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class LinkIdWithPatientUpdate : Update<LinkIdWithPatientModel, LinkIdWithPatientEvent, LinkIdWithPatientEffect> {

  override fun update(model: LinkIdWithPatientModel, event: LinkIdWithPatientEvent): Next<LinkIdWithPatientModel, LinkIdWithPatientEffect> {
    return when (event) {
      is LinkIdWithPatientViewShown -> dispatch(GetPatientNameFromId(event.patientUuid))
      LinkIdWithPatientCancelClicked -> dispatch(CloseSheetWithOutIdLinked)
      IdentifierAddedToPatient -> next(model.linkedIdToPatient(), CloseSheetWithLinkedId)
      LinkIdWithPatientAddClicked -> next(model.linkingIdToPatient(), AddIdentifierToPatient(
          patientUuid = model.patientUuid!!,
          identifier = model.identifier!!
      ))
      is PatientNameReceived -> next(model.patientNameFetched(event.patientName))
    }
  }
}
