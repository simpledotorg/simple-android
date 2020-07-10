package org.simple.clinic.location

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.ElapsedRealtimeClock
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.timer
import java.time.Duration
import javax.inject.Inject

class ScreenLocationUpdates @Inject constructor(
    private val locationRepository: LocationRepository,
    private val clock: ElapsedRealtimeClock,
    private val runtimePermissions: RuntimePermissions,
    private val schedulers: SchedulersProvider
) {

  fun streamUserLocation(
      updateInterval: Duration,
      timeout: Duration,
      discardOlderThan: Duration
  ): Observable<LocationUpdate> {
    val permissionStream = Observable
        .just(runtimePermissions.check(LOCATION_PERMISSION))
        .cache()

    val deniedFallbackStream = permissionStream
        .filter { it == DENIED }
        .map { Unavailable }

    val permissionGrantedStream = permissionStream.filter { it == GRANTED }

    val locationUpdates = permissionGrantedStream
        .switchMap { fetchLocationUpdates(updateInterval, discardOlderThan) }
        .share()

    val timeoutFallbackStream = permissionGrantedStream
        .switchMap { waitForTimeout(timeout) }
        .takeUntil(locationUpdates)

    return Observable.merge(deniedFallbackStream, locationUpdates, timeoutFallbackStream)
  }

  @SuppressLint("MissingPermission")
  private fun fetchLocationUpdates(
      updateInterval: Duration,
      discardOlderThan: Duration
  ): Observable<LocationUpdate> {
    return locationRepository
        .streamUserLocation(updateInterval, schedulers.io())
        .filter { it.isRecent(clock, discardOlderThan) }
  }

  private fun waitForTimeout(
      timeout: Duration
  ): Observable<Unavailable> {
    return Observables
        .timer(timeout, schedulers.computation())
        .map { Unavailable }
  }
}
