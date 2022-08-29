package org.simple.clinic.registration.location

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.activity.permissions.ActivityPermissionResult
import org.simple.clinic.activity.permissions.RequestPermissions
import org.simple.clinic.activity.permissions.RuntimePermissions
import org.simple.clinic.databinding.ScreenRegistrationLocationPermissionBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.registration.facility.RegistrationFacilitySelectionScreen
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class RegistrationLocationPermissionScreen : BaseScreen<
    RegistrationLocationPermissionScreen.Key,
    ScreenRegistrationLocationPermissionBinding,
    RegistrationLocationPermissionModel,
    RegistrationLocationPermissionEvent,
    RegistrationLocationPermissionEffect,
    RegistrationLocationPermissionViewEffect>(), RegistrationLocationPermissionUi {

  private val allowAccessButton
    get() = binding.allowAccessButton

  private val skipButton
    get() = binding.skipButton

  private val toolbar
    get() = binding.toolbar

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  lateinit var effectHandlerFactory: RegistrationLocationPermissionEffectHandler.Factory

  @Inject
  lateinit var screenResults: ScreenResultBus

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = ScreenRegistrationLocationPermissionBinding
      .inflate(layoutInflater, container, false)

  override fun createEffectHandler(viewEffectsConsumer: Consumer<RegistrationLocationPermissionViewEffect>) = effectHandlerFactory
      .create(viewEffectsConsumer)
      .build()

  override fun createInit() = RegistrationLocationPermissionInit()

  override fun createUpdate() = RegistrationLocationPermissionUpdate()

  override fun defaultModel() = RegistrationLocationPermissionModel.create(screenKey.ongoingRegistrationEntry)

  override fun viewEffectHandler() = RegistrationLocationPermissionViewEffectHandler(this)

  override fun events(): Observable<RegistrationLocationPermissionEvent> {
    val permissionResults = screenResults
        .streamResults()
        .ofType<ActivityPermissionResult>()

    return Observable
        .merge(
            allowLocationClicks(),
            skipClicks()
        )
        .compose(RequestPermissions<UiEvent>(runtimePermissions, permissionResults))
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun uiRenderer() = RegistrationLocationPermissionUiRenderer(this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    toolbar.setNavigationOnClickListener {
      router.pop()
    }
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
    router.push(RegistrationFacilitySelectionScreen.Key(registrationEntry))
  }

  interface Injector {
    fun inject(target: RegistrationLocationPermissionScreen)
  }

  @Parcelize
  data class Key(
      val ongoingRegistrationEntry: OngoingRegistrationEntry,
      override val analyticsName: String = "Registration Location Permission"
  ) : ScreenKey() {

    override fun instantiateFragment() = RegistrationLocationPermissionScreen()
  }
}
