package org.simple.clinic.facility

import io.reactivex.Observable
import java.util.UUID
import javax.inject.Inject

class FacilityRepository @Inject constructor() {

  fun currentFacility(): Observable<Facility> {
    return Observable.just(DUMMY_FACILITY)
  }

  companion object {
    private val DUMMY_FACILITY = Facility(UUID.randomUUID(), "TODO", "TODO")
  }
}
