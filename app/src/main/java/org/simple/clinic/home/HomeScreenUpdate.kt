package org.simple.clinic.home

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class HomeScreenUpdate : Update<HomeScreenModel, HomeScreenEvent, HomeScreenEffect> {
  override fun update(model: HomeScreenModel, event: HomeScreenEvent): Next<HomeScreenModel, HomeScreenEffect> {
    return when (event) {
      HomeFacilitySelectionClicked -> dispatch(OpenFacilitySelection)
    }
  }
}
