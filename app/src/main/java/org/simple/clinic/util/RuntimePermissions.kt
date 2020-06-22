package org.simple.clinic.util

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.router.screen.ActivityPermissionResult
import javax.inject.Inject

class RuntimePermissions @Inject constructor(
    private val activity: AppCompatActivity
) {

  fun check(permission: String): RuntimePermissionResult {
    val permissionGranted = ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    return if (permissionGranted) GRANTED else DENIED
  }

  fun request(permission: String, requestCode: Int) {
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
        .doOnNext { event -> event.permission = Optional.of(runtimePermissions.check(event.permissionString)) }
        .doOnNext { event ->
          when (event.permission.get()) {
            DENIED -> {
              // This means that the permission needs to be requested
              // since the framework runtime permissions framework
              // does not have a way for us to check if the user has
              // ever been asked for this permission before.
              val permission = event.permissionString
              val requestCode = event.permissionRequestCode

              inFlightPermissionRequests = inFlightPermissionRequests + (requestCode to event)
              runtimePermissions.request(permission, requestCode)
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
            event.permission = Optional.of(runtimePermissions.check(event.permissionString))
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

          if (permissionResult.isPresent()) {
            Analytics.reportPermissionResult(event.permissionString, permissionResult.get())
          }
        }
        .flatMap { Observable.empty<T>() }
  }
}
