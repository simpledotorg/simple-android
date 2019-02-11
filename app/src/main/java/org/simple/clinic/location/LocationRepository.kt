package org.simple.clinic.location

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.reactivex.Observable
import org.threeten.bp.Duration
import javax.inject.Inject

typealias LocationProvider = FusedLocationProviderClient

@SuppressLint("MissingPermission")
class LocationRepository @Inject constructor(private val appContext: Application) {

  data class LocationStatus(val isUsable: Boolean)

  fun streamUserLocation(updateInterval: Duration): Observable<LocationUpdate> {
    val locationProvider = LocationServices.getFusedLocationProviderClient(appContext)
    val request = locationRequest(updateInterval)

    val availableStream = locationUpdates(locationProvider, request)
        .map { LocationUpdate.Available(it) }

    val unavailableStream = locationStatusChanges(locationProvider, request)
        .filter { it.isUsable.not() }
        .map { LocationUpdate.Unavailable }

    return Observable.merge(availableStream, unavailableStream)
  }

  private fun locationRequest(updateInterval: Duration): LocationRequest {
    return LocationRequest().apply {
      priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
      interval = updateInterval.toMillis()
    }
  }

  private fun locationUpdates(provider: LocationProvider, request: LocationRequest): Observable<Coordinates> {
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
        .map { Coordinates(it.lastLocation.latitude, it.lastLocation.longitude) }
        .startWith(lastKnownLocation(provider))
  }

  private fun lastKnownLocation(provider: LocationProvider): Observable<Coordinates> {
    return Observable.create<Coordinates> { emitter ->
      provider.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
          emitter.onNext(Coordinates(location.latitude, location.longitude))
        }
        emitter.onComplete()
      }
    }
  }

  private fun locationStatusChanges(provider: LocationProvider, request: LocationRequest): Observable<LocationStatus> {
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
