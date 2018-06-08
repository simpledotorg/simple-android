package org.resolvetosavelives.red.facility

import io.reactivex.Observable
import javax.inject.Inject

class FacilityRepository @Inject constructor() {

  fun currentFacility(): Observable<Facility> {
    return Observable.just(Facility("TODO", "TODO"))
  }
}
