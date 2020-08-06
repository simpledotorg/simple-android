package org.simple.clinic.home

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class HomeScreenUpdate : Update<HomeScreenModel, HomeScreenEvent, HomeScreenEffect> {
  override fun update(model: HomeScreenModel, event: HomeScreenEvent): Next<HomeScreenModel, HomeScreenEffect> {
    return noChange()
  }
}
