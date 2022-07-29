package org.simple.clinic.home.overdue.search

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class OverdueSearchInit(
    private val isOverdueSearchV2Enabled: Boolean
) : Init<OverdueSearchModel, OverdueSearchEffect> {

  override fun init(model: OverdueSearchModel): First<OverdueSearchModel, OverdueSearchEffect> {
    val effects = mutableSetOf<OverdueSearchEffect>()
    if (isOverdueSearchV2Enabled) {
      effects.add(LoadVillageAndPatientNames)
    } else {
      effects.add(LoadOverdueSearchHistory)
    }

    effects.add(LoadSelectedOverdueAppointmentIds)

    return first(model, effects)
  }
}
