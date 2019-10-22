package org.simple.clinic.registration.confirmpin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.registration.location.RegistrationLocationPermissionScreenKey
import org.simple.clinic.registration.pin.RegistrationPinScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class RegistrationConfirmPinScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationConfirmPinScreenController

  private val backButton by bindView<ImageButton>(R.id.registrationconfirmpin_back)
  private val confirmPinEditText by bindView<EditText>(R.id.registrationconfirmpin_pin)
  private val pinHintTextView by bindView<TextView>(R.id.registrationconfirmpin_pin_hint)
  private val errorStateViewGroup by bindView<LinearLayout>(R.id.registrationconfirmpin_error)
  private val resetPinButton by bindView<Button>(R.id.registrationconfirmpin_reset_pin)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

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
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    // Showing the keyboard again in case the user returns from location permission screen.
    confirmPinEditText.showKeyboard()
  }

  private fun screenCreates() = Observable.just(RegistrationConfirmPinScreenCreated())

  private fun confirmPinTextChanges() =
      RxTextView.textChanges(confirmPinEditText)
          .map(CharSequence::toString)
          .map(::RegistrationConfirmPinTextChanged)

  private fun resetPinClicks() =
      RxView.clicks(resetPinButton)
          .map { RegistrationResetPinClicked() }

  private fun doneClicks() =
      RxTextView
          .editorActions(confirmPinEditText) { it == EditorInfo.IME_ACTION_DONE }
          .map { RegistrationConfirmPinDoneClicked() }

  fun showPinMismatchError() {
    errorStateViewGroup.visibility = View.VISIBLE
    pinHintTextView.visibility = View.GONE
  }

  fun clearPin() {
    confirmPinEditText.text = null
  }

  fun openFacilitySelectionScreen() {
    screenRouter.push(RegistrationLocationPermissionScreenKey())
  }

  fun goBackToPinScreen() {
    screenRouter.push(RegistrationPinScreenKey())
  }
}
