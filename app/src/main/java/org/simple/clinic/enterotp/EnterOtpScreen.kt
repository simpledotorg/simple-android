package org.simple.clinic.enterotp

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.widget.editorActions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_enterotp.view.*
import org.simple.clinic.R
import org.simple.clinic.appconfig.Country
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class EnterOtpScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

  @Inject
  lateinit var controller: EnterOtpScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var country: Country

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<Injector>().inject(this)

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            screenCreates(),
            otpSubmits(),
            otpTextChanges(),
            backClicks(),
            resendSmsClicks()
        ),
        controller = controller,
        screenDestroys = detaches().map { ScreenDestroyed() }
    )

    otpEntryEditText.showKeyboard()
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun backClicks() = backButton.clicks().map { EnterOtpBackClicked() }

  private fun otpSubmits() =
      otpEntryEditText.editorActions() { it == EditorInfo.IME_ACTION_DONE }
          .map { EnterOtpSubmitted(otpEntryEditText.text.toString()) }

  private fun resendSmsClicks() =
      resendSmsButton.clicks().map { EnterOtpResendSmsClicked() }

  private fun otpTextChanges() =
      otpEntryEditText.textChanges().map { EnterOtpTextChanges(it.toString()) }

  fun showUserPhoneNumber(phoneNumber: String) {
    val phoneNumberWithCountryCode = resources.getString(
        R.string.enterotp_phonenumber,
        country.isdCode,
        phoneNumber
    )

    userPhoneNumberTextView.text = phoneNumberWithCountryCode
  }

  fun goBack() {
    hideKeyboard()
    screenRouter.pop()
  }

  fun showUnexpectedError() {
    showError(resources.getString(R.string.api_unexpected_error))
  }

  fun showNetworkError() {
    showError(resources.getString(R.string.api_network_error))
  }

  fun showServerError(error: String) {
    showError(error)
    otpEntryEditText.showKeyboard()
  }

  fun showIncorrectOtpError() {
    showError(resources.getString(R.string.enterotp_incorrect_code))
    otpEntryEditText.showKeyboard()
  }

  private fun showError(error: String) {
    smsSentTextView.visibility = View.GONE
    errorTextView.text = error
    errorTextView.visibility = View.VISIBLE
  }

  fun hideError() {
    errorTextView.visibility = View.GONE
  }

  fun showProgress() {
    TransitionManager.beginDelayedTransition(this)
    validateOtpProgressBar.visibility = View.VISIBLE
    otpEntryContainer.visibility = View.INVISIBLE
  }

  fun hideProgress() {
    TransitionManager.beginDelayedTransition(this)
    validateOtpProgressBar.visibility = View.INVISIBLE
    otpEntryContainer.visibility = View.VISIBLE
  }

  fun showSmsSentMessage() {
    smsSentTextView.visibility = View.VISIBLE
  }

  fun clearPin() {
    otpEntryEditText.text = null
  }

  interface Injector {
    fun inject(target: EnterOtpScreen)
  }
}
