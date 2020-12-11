package org.simple.clinic.shortcodesearchresult

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class ShortCodeSearchResultUpdate: Update<ShortCodeSearchResultState, ShortCodeSearchResultEvent, ShortCodeSearchResultEffect> {

  override fun update(
      model: ShortCodeSearchResultState,
      event: ShortCodeSearchResultEvent
  ): Next<ShortCodeSearchResultState, ShortCodeSearchResultEffect> {
    return when(event) {
      is ViewPatient -> dispatch(OpenPatientSummary(event.patientUuid))
      SearchPatient -> noChange()
    }
  }
}
