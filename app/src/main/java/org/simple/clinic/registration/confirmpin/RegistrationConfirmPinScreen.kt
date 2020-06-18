package org.simple.clinic.registration.confirmpin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.widget.editorActions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_registration_confirm_pin.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.SECURITY_PIN_LENGTH
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.registration.location.RegistrationLocationPermissionScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class RegistrationConfirmPinScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), RegistrationConfirmPinUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationConfirmPinScreenController

  private val events by unsafeLazy {
    Observable
        .merge(
            screenCreates(),
            confirmPinTextChanges(),
            resetPinClicks(),
            doneClicks()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    backButton.setOnClickListener {
      screenRouter.pop()
    }

    // Because PIN is auto-submitted when 4 digits are entered, restoring the
    // existing PIN will immediately take the user to the next screen.
    confirmPinEditText.isSaveEnabled = false

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = detaches().map { ScreenDestroyed() }
    )

    // Showing the keyboard again in case the user returns from location permission screen.
    confirmPinEditText.showKeyboard()
  }

  private fun screenCreates() = Observable.just(RegistrationConfirmPinScreenCreated())

  private fun confirmPinTextChanges() =
      confirmPinEditText
          .textChanges()
          .map(CharSequence::toString)
          .map(::RegistrationConfirmPinTextChanged)

  private fun resetPinClicks() =
      resetPinButton
          .clicks()
          .map { RegistrationResetPinClicked() }

  private fun doneClicks(): Observable<RegistrationConfirmPinDoneClicked>? {
    val imeDoneClicks = confirmPinEditText
        .editorActions() { it == EditorInfo.IME_ACTION_DONE }
        .map { RegistrationConfirmPinDoneClicked() }

    val pinAutoSubmits = confirmPinEditText
        .textChanges()
        .skip(1)
        .filter { it.length == SECURITY_PIN_LENGTH }
        .map { RegistrationConfirmPinDoneClicked() }

    return imeDoneClicks.mergeWith(pinAutoSubmits)
  }

  override fun showPinMismatchError() {
    errorStateViewGroup.visibility = View.VISIBLE
    pinHintTextView.visibility = View.GONE
  }

  override fun clearPin() {
    confirmPinEditText.text = null
  }

  override fun openFacilitySelectionScreen() {
    hideKeyboard()
    screenRouter.push(RegistrationLocationPermissionScreenKey())
  }

  override fun goBackToPinScreen() {
    screenRouter.pop()
  }

  interface Injector {
    fun inject(target: RegistrationConfirmPinScreen)
  }
}
