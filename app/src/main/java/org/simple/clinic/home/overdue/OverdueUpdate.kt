package org.simple.clinic.home.overdue

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next
import java.time.LocalDate

class OverdueUpdate(
    val date: LocalDate
) : Update<OverdueModel, OverdueEvent, OverdueEffect> {

  override fun update(model: OverdueModel, event: OverdueEvent): Next<OverdueModel, OverdueEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
    }
  }
}
