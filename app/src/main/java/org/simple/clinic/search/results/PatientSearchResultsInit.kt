package org.simple.clinic.search.results

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class PatientSearchResultsInit : Init<PatientSearchResultsModel, PatientSearchResultsEffect> {

  override fun init(model: PatientSearchResultsModel): First<PatientSearchResultsModel, PatientSearchResultsEffect> {
    return first(model)
  }
}
