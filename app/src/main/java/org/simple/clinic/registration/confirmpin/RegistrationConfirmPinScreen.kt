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
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.registration.location.RegistrationLocationPermissionScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class RegistrationConfirmPinScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationConfirmPinScreenController

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
        events = Observable.merge(
            screenCreates(),
            confirmPinTextChanges(),
            resetPinClicks(),
            doneClicks()
        ),
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

  private fun doneClicks() =
      confirmPinEditText
          .editorActions() { it == EditorInfo.IME_ACTION_DONE }
          .map { RegistrationConfirmPinDoneClicked() }

  fun showPinMismatchError() {
    errorStateViewGroup.visibility = View.VISIBLE
    pinHintTextView.visibility = View.GONE
  }

  fun clearPin() {
    confirmPinEditText.text = null
  }

  fun openFacilitySelectionScreen() {
    hideKeyboard()
    screenRouter.push(RegistrationLocationPermissionScreenKey())
  }

  fun goBackToPinScreen() {
    screenRouter.pop()
  }

  interface Injector {
    fun inject(target: RegistrationConfirmPinScreen)
  }
}
