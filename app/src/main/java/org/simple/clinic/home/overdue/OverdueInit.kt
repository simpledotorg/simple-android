package org.simple.clinic.home.overdue

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import java.time.LocalDate

class OverdueInit(
    private val date: LocalDate
) : Init<OverdueModel, OverdueEffect> {

  override fun init(model: OverdueModel): First<OverdueModel, OverdueEffect> {
    val effects = mutableSetOf<OverdueEffect>()

    if (!model.hasLoadedCurrentFacility) {
      effects.add(LoadCurrentFacility)
    } else {
      effects.add(LoadOverdueAppointments(date, model.facility!!))
    }

    return first(model, effects)
  }
}
