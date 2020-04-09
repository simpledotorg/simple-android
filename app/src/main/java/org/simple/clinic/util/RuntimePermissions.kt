package org.simple.clinic.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.util.RuntimePermissionResult.DENIED
import org.simple.clinic.util.RuntimePermissionResult.GRANTED
import javax.inject.Inject

enum class RuntimePermissionResult {
  GRANTED,
  DENIED
}

class RuntimePermissions @Inject constructor() {

  fun check(activity: Activity, permission: String): RuntimePermissionResult {
    val permissionGranted = ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    return if (permissionGranted) GRANTED else DENIED
  }

  fun request(activity: Activity, permission: String, requestCode: Int) {
    ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
  }
}

interface RequiresPermission {
  var permission: Optional<RuntimePermissionResult>
  val permissionString: String
  val permissionRequestCode: Int

  val isPermissionGranted: Boolean
    get() = permission == Just(GRANTED)
}

class RequestPermissions<T : Any>(
    private val runtimePermissions: RuntimePermissions,
    private val activity: Activity,
    private val permissionResults: Observable<ActivityPermissionResult>
) : ObservableTransformer<T, T> {

  private val permissionCompletedEvents = PublishSubject.create<T>()
  private var inFlightPermissionRequests = mapOf<Int, RequiresPermission>()

  override fun apply(upstream: Observable<T>): ObservableSource<T> {
    val sharedUpstream = upstream.share()

    val eventsRequiringPermission = sharedUpstream.ofType<RequiresPermission>()
    val eventsNotRequiringPermission = sharedUpstream.filter { it !is RequiresPermission }

    return Observable.mergeArray(
        eventsNotRequiringPermission,
        permissionCompletedEvents,
        requestPermissions(eventsRequiringPermission),
        handlePermissionResults(),
        reportPermissionResultsToAnalytics()
    )
  }

  @Suppress("UNCHECKED_CAST")
  private fun requestPermissions(
      events: Observable<RequiresPermission>
  ): Observable<T> {
    return events
        .cast<RequiresPermission>()
        .doOnNext { event -> event.permission = Just(runtimePermissions.check(activity, event.permissionString)) }
        .doOnNext { event ->
          when ((event.permission as Just).value) {
            DENIED -> {
              // This means that the permission needs to be requested
              // since the framework runtime permissions framework
              // does not have a way for us to check if the user has
              // ever been asked for this permission before.
              val permission = event.permissionString
              val requestCode = event.permissionRequestCode

              inFlightPermissionRequests = inFlightPermissionRequests + (requestCode to event)
              runtimePermissions.request(activity, permission, requestCode)
            }
            GRANTED -> permissionCompletedEvents.onNext(event as T)
          }
        }
        .flatMap { Observable.empty<T>() }
  }

  @Suppress("UNCHECKED_CAST")
  private fun handlePermissionResults(): Observable<T> {
    return permissionResults
        .map { it.requestCode }
        .doOnNext { requestCode ->
          val event = inFlightPermissionRequests[requestCode]

          if (event != null) {
            event.permission = Just(runtimePermissions.check(activity, event.permissionString))
            permissionCompletedEvents.onNext(event as T)
          }
        }
        .doOnNext { requestCode ->
          inFlightPermissionRequests = inFlightPermissionRequests - requestCode
        }
        .flatMap { Observable.empty<T>() }
  }

  private fun reportPermissionResultsToAnalytics(): Observable<T> {
    return permissionCompletedEvents
        .ofType<RequiresPermission>()
        .doOnNext { event ->
          val permissionResult = event.permission

          if(permissionResult is Just) {
            Analytics.reportPermissionResult(event.permissionString, permissionResult.value)
          }
        }
        .flatMap { Observable.empty<T>() }
  }
}
