package org.simple.clinic.registration.pin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_registration_pin.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.main.TheActivity
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import javax.inject.Inject

class RegistrationPinScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), RegistrationPinUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationPinScreenController

  private val events by unsafeLazy {
    Observable
        .merge(
            screenCreates(),
            pinTextChanges(),
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
    TheActivity.component.inject(this)

    backButton.setOnClickListener {
      screenRouter.pop()
    }

    // Because PIN is auto-submitted when 4 digits are entered, restoring the
    // existing PIN will immediately take the user to the next screen.
    pinEditText.isSaveEnabled = false

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    post { pinEditText.requestFocus() }
  }

  private fun screenCreates() = Observable.just(RegistrationPinScreenCreated())

  private fun pinTextChanges() =
      RxTextView.textChanges(pinEditText)
          .map(CharSequence::toString)
          .map(::RegistrationPinTextChanged)

  private fun doneClicks() =
      RxTextView
          .editorActions(pinEditText) { it == EditorInfo.IME_ACTION_DONE }
          .map { RegistrationPinDoneClicked() }

  override fun showIncompletePinError() {
    pinHintTextView.visibility = View.GONE
    errorTextView.visibility = View.VISIBLE
    errorTextView.text = resources.getString(R.string.registrationpin_error_incomplete_pin)
  }

  override fun hideIncompletePinError() {
    pinHintTextView.visibility = View.VISIBLE
    errorTextView.visibility = View.GONE
  }

  override fun openRegistrationConfirmPinScreen() {
    screenRouter.push(RegistrationConfirmPinScreenKey())
  }
}
