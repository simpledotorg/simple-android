package org.simple.clinic.registration.location

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenRegistrationLocationPermissionBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.registration.facility.RegistrationFacilitySelectionScreenKey
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class RegistrationLocationPermissionScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), RegistrationLocationPermissionUi {

  var binding: ScreenRegistrationLocationPermissionBinding? = null

  private val allowAccessButton
    get() = binding!!.allowAccessButton

  private val skipButton
    get() = binding!!.skipButton

  private val toolbar
    get() = binding!!.toolbar

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  lateinit var effectHandlerFactory: RegistrationLocationPermissionEffectHandler.Factory

  @Inject
  lateinit var screenResults: ScreenResultBus

  private val screenKey by unsafeLazy { screenKeyProvider.keyFor<RegistrationLocationPermissionScreenKey>(this) }

  private val events by unsafeLazy {
    val permissionResults = screenResults
        .streamResults()
        .ofType<ActivityPermissionResult>()

    Observable
        .merge(
            allowLocationClicks(),
            skipClicks()
        )
        .compose(RequestPermissions<UiEvent>(runtimePermissions, permissionResults))
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val uiRenderer = RegistrationLocationPermissionUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = RegistrationLocationPermissionModel.create(screenKey.ongoingRegistrationEntry),
        update = RegistrationLocationPermissionUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = RegistrationLocationPermissionInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    binding = ScreenRegistrationLocationPermissionBinding.bind(this)
    if (isInEditMode) {
      return
    }
    context.injector<Injector>().inject(this)

    toolbar.setOnClickListener {
      router.pop()
    }

    // Can't tell why, but the keyboard stays
    // visible on coming from the previous screen.
    hideKeyboard()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun allowLocationClicks(): Observable<RegistrationLocationPermissionEvent> {
    return allowAccessButton
        .clicks()
        .map { RequestLocationPermission() }
  }

  private fun skipClicks(): Observable<RegistrationLocationPermissionEvent> {
    return skipButton
        .clicks()
        .map { SkipClicked }
  }

  override fun openFacilitySelectionScreen(registrationEntry: OngoingRegistrationEntry) {
    router.push(RegistrationFacilitySelectionScreenKey(registrationEntry).wrap())
  }

  interface Injector {
    fun inject(target: RegistrationLocationPermissionScreen)
  }
}
