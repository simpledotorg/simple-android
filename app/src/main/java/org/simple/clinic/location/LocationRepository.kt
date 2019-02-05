package org.simple.clinic.location

import io.reactivex.Observable
import org.threeten.bp.Duration
import javax.inject.Inject

class LocationRepository @Inject constructor() {

  fun streamUserLocation(updateInterval: Duration): Observable<LocationUpdate> {
    // TODO.
    return Observable.just(LocationUpdate.TurnedOff)
  }
}
