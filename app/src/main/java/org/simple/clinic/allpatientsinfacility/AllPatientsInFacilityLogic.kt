package org.simple.clinic.allpatientsinfacility

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import org.simple.clinic.mobius.next

fun allPatientsInFacilityInit(
    model: AllPatientsInFacilityModel
) : First<AllPatientsInFacilityModel, AllPatientsInFacilityEffect> {
  return first(model, setOf(FetchFacilityEffect))
}

fun allPatientsInFacilityUpdate(
    model: AllPatientsInFacilityModel,
    event: AllPatientsInFacilityEvent
): Next<AllPatientsInFacilityModel, AllPatientsInFacilityEffect> {
  return when (event) {
    is FacilityFetchedEvent -> {
      val facility = event.facility
      next(model.facilityFetched(facility), FetchPatientsEffect(facility))
    }

    is NoPatientsInFacilityEvent -> next(model.noPatients())

    is HasPatientsInFacilityEvent -> next(model.hasPatients(event.patients))
  }
}
