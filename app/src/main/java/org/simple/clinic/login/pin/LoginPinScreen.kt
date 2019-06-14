package org.simple.clinic.login.pin

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.security.pin.PinAuthenticated
import org.simple.clinic.security.pin.PinDigestToVerify
import org.simple.clinic.security.pin.PinEntryCardView
import org.simple.clinic.security.pin.PinEntryCardView.State
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class LoginPinScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: LoginPinScreenController

  private val phoneNumberTextView by bindView<TextView>(R.id.loginpin_phone_number)
  private val backButton by bindView<ImageButton>(R.id.loginpin_back)
  private val pinEntryCardView by bindView<PinEntryCardView>(R.id.loginpin_pin_entry_card)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    pinEntryCardView.setForgotButtonVisible(false)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            pinAuthentications(),
            backClicks(),
            otpReceived()
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun screenCreates(): Observable<UiEvent> {
    return Observable.just(PinScreenCreated())
  }

  private fun pinAuthentications() =
      pinEntryCardView
          .downstreamUiEvents
          .ofType<PinAuthenticated>()
          .map { LoginPinAuthenticated(it.pin) }

  private fun backClicks(): Observable<PinBackClicked> {
    val backClicksFromView = RxView.clicks(backButton).map { PinBackClicked() }

    val backClicksFromSystem = Observable.create<PinBackClicked> { emitter ->
      val backPressInterceptor = object : BackPressInterceptor {
        override fun onInterceptBackPress(callback: BackPressInterceptCallback) {
          emitter.onNext(PinBackClicked())
        }
      }

      screenRouter.registerBackPressInterceptor(backPressInterceptor)

      emitter.setCancellable { screenRouter.unregisterBackPressInterceptor(backPressInterceptor) }
    }

    return backClicksFromView.mergeWith(backClicksFromSystem)
  }

  private fun otpReceived(): Observable<LoginPinOtpReceived>? {
    val key = screenRouter.key<LoginPinScreenKey>(this)
    return Observable.just(LoginPinOtpReceived(key.otp))
  }

  fun showPhoneNumber(phoneNumber: String) {
    phoneNumberTextView.text = phoneNumber
  }

  private fun showError(@StringRes errorRes: Int) {
    pinEntryCardView.moveToState(State.PinEntry)
    pinEntryCardView.showError(context.getString(errorRes))
  }

  fun showNetworkError() {
    showError(R.string.loginpin_error_check_internet_connection)
  }

  fun showUnexpectedError() {
    showError(R.string.api_unexpected_error)
  }

  fun openHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey(), RouterDirection.REPLACE)
  }

  fun goBackToRegistrationScreen() {
    screenRouter.pop()
  }

  fun submitWithPinDigest(pinDigest: String) {
    pinEntryCardView.upstreamUiEvents.onNext(PinDigestToVerify(pinDigest))
  }
}
