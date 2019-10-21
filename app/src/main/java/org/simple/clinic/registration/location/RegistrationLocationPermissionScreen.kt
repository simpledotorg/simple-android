package org.simple.clinic.registration.location

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.location.LOCATION_PERMISSION
import org.simple.clinic.registration.facility.RegistrationFacilitySelectionScreenKey
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

private const val REQUESTCODE_LOCATION_PERMISSION = 0

class RegistrationLocationPermissionScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

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

    bindUiToController(
        ui = this,
        events = locationPermissionChanges(),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

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

  private fun locationPermissionChanges(): Observable<UiEvent> {
    return screenRouter.streamScreenResults()
        .ofType<ActivityPermissionResult>()
        .filter { result -> result.requestCode == REQUESTCODE_LOCATION_PERMISSION }
        .map { RuntimePermissions.check(activity, LOCATION_PERMISSION) }
        .map(::RegistrationLocationPermissionChanged)
  }

  private fun requestLocationPermission() {
    RuntimePermissions.request(activity, LOCATION_PERMISSION, REQUESTCODE_LOCATION_PERMISSION)
  }

  fun openFacilitySelectionScreen() {
    screenRouter.push(RegistrationFacilitySelectionScreenKey())
  }
}
