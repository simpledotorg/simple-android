package org.simple.clinic.home.overdue.search

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class OverdueSearchInit : Init<OverdueSearchModel, OverdueSearchEffect> {

  override fun init(model: OverdueSearchModel): First<OverdueSearchModel, OverdueSearchEffect> {
    return first(model, setOf(LoadVillageAndPatientNames, LoadSelectedOverdueAppointmentIds))
  }
}
