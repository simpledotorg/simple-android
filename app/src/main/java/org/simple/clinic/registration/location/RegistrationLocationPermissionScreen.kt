package org.simple.clinic.registration.location

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_registration_location_permission.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.main.TheActivity
import org.simple.clinic.registration.facility.RegistrationFacilitySelectionScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class RegistrationLocationPermissionScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), UiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationLocationPermissionScreenController

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  private val events by unsafeLazy {
    allowLocationClicks()
        .compose(runtimePermissions())
        .compose(ReportAnalyticsEvents())
        .share()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    toolbar.setOnClickListener {
      screenRouter.pop()
    }

    skipButton.setOnClickListener {
      openFacilitySelectionScreen()
    }

    // Can't tell why, but the keyboard stays
    // visible on coming from the previous screen.
    hideKeyboard()
  }

  private fun allowLocationClicks(): Observable<UiEvent> = allowAccessButton.clicks().map { RequestLocationPermission() }

  private fun runtimePermissions(): RequestPermissions<UiEvent> = RequestPermissions(runtimePermissions, screenRouter.streamScreenResults().ofType())

  override fun openFacilitySelectionScreen() {
    screenRouter.push(RegistrationFacilitySelectionScreenKey())
  }
}
