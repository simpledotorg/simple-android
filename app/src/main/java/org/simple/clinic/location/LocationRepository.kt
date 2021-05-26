package org.simple.clinic.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.reactivex.Observable
import io.reactivex.Scheduler
import org.simple.clinic.location.LocationUpdate.Available
import java.time.Duration
import javax.inject.Inject

typealias LocationProvider = FusedLocationProviderClient

const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

@SuppressLint("MissingPermission")
class LocationRepository @Inject constructor(private val appContext: Application) {

  data class LocationStatus(val isUsable: Boolean)

  /**
   * @param updateScheduler [FusedLocationProviderClient] requires a looper to emit location updates.
   * The easiest way to get one is from the Activity, but that results in the updates being received
   * on the main thread. This param makes it explicit for the caller to think about this.
   */
  @RequiresPermission(LOCATION_PERMISSION)
  fun streamUserLocation(
      updateInterval: Duration,
      updateScheduler: Scheduler
  ): Observable<LocationUpdate> {
    val locationProvider = LocationServices.getFusedLocationProviderClient(appContext)
    val request = locationRequest(updateInterval)

    val availableStream = locationUpdates(locationProvider, request)
    val unavailableStream = locationStatusChanges(locationProvider, request)
        .filter { it.isUsable.not() }
        .map { LocationUpdate.Unavailable }

    return Observable
        .merge(availableStream, unavailableStream)
        .observeOn(updateScheduler)
  }

  private fun locationRequest(updateInterval: Duration): LocationRequest {
    return LocationRequest().apply {
      priority = LocationRequest.PRIORITY_HIGH_ACCURACY
      interval = updateInterval.toMillis()
    }
  }

  private fun locationUpdates(
      provider: LocationProvider,
      request: LocationRequest
  ): Observable<Available> {
    val locationChanges = Observable.create<LocationResult> { emitter ->
      val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
          emitter.onNext(result)
        }
      }

      emitter.setCancellable { provider.removeLocationUpdates(locationCallback) }
      provider.requestLocationUpdates(request, locationCallback, appContext.mainLooper)
    }

    return locationChanges
        .filter { it.lastLocation != null }
        .map { it.lastLocation }
        .map {
          // Location#time would have been easier to work with instead of nanos-elapsed-since-boot,
          // but during our testing, the time was off by 12 hours. We only tested on the emulator.
          Available(
              location = Coordinates(it.latitude, it.longitude),
              timeSinceBootWhenRecorded = Duration.ofNanos(it.elapsedRealtimeNanos))
        }
        .startWith(lastKnownLocation(provider))
  }

  private fun lastKnownLocation(provider: LocationProvider): Observable<Available> {
    return Observable.create { emitter ->
      provider.lastLocation.addOnSuccessListener { location: Location? ->
        location?.run {
          emitter.onNext(Available(
              location = Coordinates(latitude, longitude),
              timeSinceBootWhenRecorded = Duration.ofNanos(elapsedRealtimeNanos)))
        }
        emitter.onComplete()
      }
    }
  }

  private fun locationStatusChanges(
      provider: LocationProvider,
      request: LocationRequest
  ): Observable<LocationStatus> {
    return Observable.create { emitter ->
      val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(availability: LocationAvailability) {
          emitter.onNext(LocationStatus(isUsable = availability.isLocationAvailable))
        }
      }
      emitter.setCancellable { provider.removeLocationUpdates(locationCallback) }
      provider.requestLocationUpdates(request, locationCallback, appContext.mainLooper)
    }
  }
}
