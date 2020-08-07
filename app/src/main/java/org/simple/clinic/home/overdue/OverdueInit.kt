package org.simple.clinic.home.overdue

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class OverdueInit : Init<OverdueModel, OverdueEffect> {

  override fun init(model: OverdueModel): First<OverdueModel, OverdueEffect> {
    return first(model, LoadCurrentFacility)
  }
}
