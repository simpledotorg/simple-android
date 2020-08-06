package org.simple.clinic.home

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class HomeScreenInit : Init<HomeScreenModel, HomeScreenEffect> {
  override fun init(model: HomeScreenModel): First<HomeScreenModel, HomeScreenEffect> {
    return first(model)
  }
}
