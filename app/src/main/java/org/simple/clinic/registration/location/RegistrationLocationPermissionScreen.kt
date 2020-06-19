package org.simple.clinic.registration.location

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_registration_location_permission.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.registration.facility.RegistrationFacilitySelectionScreenKey
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class RegistrationLocationPermissionScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), RegistrationLocationPermissionUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationLocationPermissionScreenController

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  private val events by unsafeLazy {
    val permissionResults = screenRouter
        .streamScreenResults()
        .ofType<ActivityPermissionResult>()

    allowLocationClicks()
        .compose(RequestPermissions<UiEvent>(runtimePermissions, permissionResults))
        .compose(ReportAnalyticsEvents())
        .share()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<Injector>().inject(this)

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = detaches().map { ScreenDestroyed() }
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

  override fun openFacilitySelectionScreen() {
    screenRouter.push(RegistrationFacilitySelectionScreenKey())
  }

  interface Injector {
    fun inject(target: RegistrationLocationPermissionScreen)
  }
}
