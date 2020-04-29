package org.simple.clinic.allpatientsinfacility

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class AllPatientsInFacilityUpdate : Update<AllPatientsInFacilityModel, AllPatientsInFacilityEvent, AllPatientsInFacilityEffect> {
  override fun update(
      model: AllPatientsInFacilityModel,
      event: AllPatientsInFacilityEvent
  ): Next<AllPatientsInFacilityModel, AllPatientsInFacilityEffect> {
    return when (event) {
      is FacilityFetchedEvent -> {
        val facility = event.facility
        next(model.facilityFetched(facility), FetchPatientsEffect(facility))
      }

      is NoPatientsInFacilityEvent -> Next.next(model.noPatients())

      is HasPatientsInFacilityEvent -> Next.next(model.hasPatients(event.patients))
    }
  }
}
