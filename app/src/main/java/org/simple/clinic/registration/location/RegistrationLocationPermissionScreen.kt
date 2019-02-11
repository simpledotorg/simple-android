package org.simple.clinic.registration.location

import android.Manifest
import android.content.Context
import androidx.appcompat.widget.Toolbar
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.registration.facility.RegistrationFacilitySelectionScreenKey
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

private const val REQUESTCODE_LOCATION_PERMISSION = 0
private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

class RegistrationLocationPermissionScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var controller: RegistrationLocationPermissionScreenController

  private val toolbar by bindView<Toolbar>(R.id.registrationlocation_toolbar)
  private val skipButton by bindView<Button>(R.id.registrationlocation_skip)
  private val allowAccessButton by bindView<Button>(R.id.registrationlocation_allow_access)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    locationPermissionChanges()
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    toolbar.setOnClickListener {
      screenRouter.pop()
    }

    skipButton.setOnClickListener {
      openFacilitySelectionScreen()
    }

    allowAccessButton.setOnClickListener {
      requestLocationPermission()
    }

    // Can't tell why, but the keyboard stays
    // visible on coming from the previous screen.
    hideKeyboard()
  }

  private fun locationPermissionChanges(): Observable<LocationPermissionChanged> {
    return screenRouter.streamScreenResults()
        .ofType<ActivityPermissionResult>()
        .filter { result -> result.requestCode == REQUESTCODE_LOCATION_PERMISSION }
        .map { RuntimePermissions.check(activity, LOCATION_PERMISSION) }
        .map(::LocationPermissionChanged)
  }

  private fun requestLocationPermission() {
    RuntimePermissions.request(activity, LOCATION_PERMISSION, REQUESTCODE_LOCATION_PERMISSION)
  }

  fun openFacilitySelectionScreen() {
    screenRouter.push(RegistrationFacilitySelectionScreenKey())
  }
}
