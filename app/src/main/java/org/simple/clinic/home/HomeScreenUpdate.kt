package org.simple.clinic.home

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.dispatch
import javax.inject.Inject

class HomeScreenUpdate @Inject constructor(
    private val features: Features
) : Update<HomeScreenModel, HomeScreenEvent, HomeScreenEffect> {

  override fun update(
      model: HomeScreenModel,
      event: HomeScreenEvent
  ): Next<HomeScreenModel, HomeScreenEffect> {
    return when (event) {
      HomeFacilitySelectionClicked -> dispatch(OpenFacilitySelection)
      is CurrentFacilityLoaded -> currentFacilityLoaded(model, event)
      is OverdueAppointmentCountLoaded -> next(model.overdueAppointmentCountLoaded(event.overdueAppointmentCount))
    }
  }

  private fun currentFacilityLoaded(
      model: HomeScreenModel,
      event: CurrentFacilityLoaded
  ): Next<HomeScreenModel, HomeScreenEffect> {
    val effects = mutableSetOf<HomeScreenEffect>()

    if (features.isEnabled(Feature.OverdueCount)) {
      effects.add(LoadOverdueAppointmentCount(event.facility))
    }

    return next(model.facilityLoaded(event.facility), effects)
  }
}
