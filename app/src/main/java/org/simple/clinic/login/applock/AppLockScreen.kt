package org.simple.clinic.login.applock

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenAppLockBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.security.pin.PinAuthenticated
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class AppLockScreen : BaseScreen<
    AppLockScreenKey,
    ScreenAppLockBinding,
    AppLockModel,
    AppLockEvent,
    AppLockEffect,
    AppLockViewEffect>(), AppLockScreenUi, AppLockUiActions, HandlesBack {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerFactory: AppLockEffectHandler.Factory

  private val logoutButton
    get() = binding.logoutButton

  private val pinEntryCardView
    get() = binding.pinEntryCardView

  private val pinEditText
    get() = binding.pinEntryCardView.pinEditText

  private val forgotPinButton
    get() = binding.pinEntryCardView.forgotPinButton

  private val fullNameTextView
    get() = binding.fullNameTextView

  private val facilityTextView
    get() = binding.facilityTextView

  private val backClicks = PublishSubject.create<AppLockBackClicked>()

  override fun defaultModel() = AppLockModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenAppLockBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = AppLockUiRenderer(this)

  override fun viewEffectHandler() = AppLockViewEffectHandler(this)

  override fun events() = Observable
      .merge(
          backClicks,
          forgotPinClicks(),
          pinAuthentications()
      )
      .compose(ReportAnalyticsEvents())
      .cast<AppLockEvent>()

  override fun createUpdate() = AppLockUpdate()

  override fun createInit() = AppLockInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<AppLockViewEffect>) = effectHandlerFactory
      .create(viewEffectsConsumer = viewEffectsConsumer)
      .build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    logoutButton.setOnClickListener {
      Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show()
    }

    // The keyboard shows up on PIN field automatically when the app is
    // starting, but not when the user comes back from FacilityChangeScreen.
    pinEditText.showKeyboard()
  }

  override fun onBackPressed(): Boolean {
    backClicks.onNext(AppLockBackClicked)
    return true
  }

  private fun forgotPinClicks() =
      forgotPinButton
          .clicks()
          .map { AppLockForgotPinClicked }

  private fun pinAuthentications() =
      pinEntryCardView
          .downstreamUiEvents
          .ofType<PinAuthenticated>()
          .map { AppLockPinAuthenticated }

  override fun setUserFullName(fullName: String) {
    fullNameTextView.text = fullName
  }

  override fun setFacilityName(facilityName: String) {
    facilityTextView.text = facilityName
  }

  override fun restorePreviousScreen() {
    router.replaceHistory(screenKey.screenHistory)
  }

  override fun exitApp() {
    activity.finish()
  }

  override fun showConfirmResetPinDialog() {
    ConfirmResetPinDialog.show(activity.supportFragmentManager)
  }

  interface Injector {
    fun inject(target: AppLockScreen)
  }
}
