package org.simple.clinic.home

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
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
      is CurrentFacilityLoaded -> next(model.facilityLoaded(event.facility))
      is OverdueAppointmentCountLoaded -> next(model.overdueAppointmentCountLoaded(event.overdueAppointmentCount))
    }
  }
}
