package org.simple.clinic.search

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class PatientSearchUpdate : Update<PatientSearchModel, PatientSearchEvent, PatientSearchEffect> {

  override fun update(model: PatientSearchModel, event: PatientSearchEvent): Next<PatientSearchModel, PatientSearchEffect> {
    return noChange()
  }
}
