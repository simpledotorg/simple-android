package org.simple.clinic.summary.linkId

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class LinkIdWithPatientUpdate : Update<LinkIdWithPatientModel, LinkIdWithPatientEvent, LinkIdWithPatientEffect> {

  override fun update(model: LinkIdWithPatientModel, event: LinkIdWithPatientEvent): Next<LinkIdWithPatientModel, LinkIdWithPatientEffect> {
    return when (event) {
      is LinkIdWithPatientViewShown -> dispatch(RenderIdentifierText(event.identifier))
      LinkIdWithPatientCancelClicked -> dispatch(CloseSheetWithOutIdLinked)
      is CurrentUserLoaded -> noChange()
      IdentifierAddedToPatient -> noChange()
    }
  }
}
