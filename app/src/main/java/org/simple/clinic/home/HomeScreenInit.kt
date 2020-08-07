package org.simple.clinic.home

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class HomeScreenInit : Init<HomeScreenModel, HomeScreenEffect> {
  override fun init(model: HomeScreenModel): First<HomeScreenModel, HomeScreenEffect> {
    return first(model, LoadCurrentFacility)
  }
}
