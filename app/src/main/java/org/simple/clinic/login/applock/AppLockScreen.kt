package org.simple.clinic.login.applock

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenAppLockBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.security.pin.PinAuthenticated
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class AppLockScreen :
    BaseScreen<
        AppLockScreenKey,
        ScreenAppLockBinding,
        AppLockModel,
        AppLockEvent,
        AppLockEffect
        >(),
    AppLockScreenUi,
    AppLockUiActions,
    HandlesBack {

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

  private val events by unsafeLazy {
    Observable
        .merge(
            backClicks,
            forgotPinClicks(),
            pinAuthentications()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = AppLockUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = AppLockModel.create(),
        init = AppLockInit(),
        update = AppLockUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun defaultModel() = AppLockModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenAppLockBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = AppLockUiRenderer(this)

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

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

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
    router.pop()
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
