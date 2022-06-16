package org.simple.clinic.home.overdue.search

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class OverdueSearchInit : Init<OverdueSearchModel, OverdueSearchEffect> {

  override fun init(model: OverdueSearchModel): First<OverdueSearchModel, OverdueSearchEffect> {
    return first(model, LoadOverdueSearchHistory)
  }
}
