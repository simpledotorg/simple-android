package org.simple.clinic.enterotp

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.StaggeredEditText
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class EnterOtpScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

  @Inject
  lateinit var controller: EnterOtpScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val userPhoneNumberTextView by bindView<TextView>(R.id.enterotp_phonenumber)
  private val otpEntryEditText by bindView<StaggeredEditText>(R.id.enterotp_otp)
  private val backButton by bindView<ImageButton>(R.id.enterotp_back)
  private val errorTextView by bindView<TextView>(R.id.enterotp_error)
  private val smsSentTextView by bindView<TextView>(R.id.enterotp_sms_sent)
  private val validateOtpProgressBar by bindView<ProgressBar>(R.id.enterotp_progress)
  private val otpEntryContainer by bindView<ViewGroup>(R.id.enterotp_otp_container)
  private val resendSmsButton by bindView<Button>(R.id.enterotp_resendsms)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

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
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    otpEntryEditText.showKeyboard()
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun backClicks() = RxView.clicks(backButton).map { EnterOtpBackClicked() }

  private fun otpSubmits() =
      RxTextView.editorActions(otpEntryEditText) { it == EditorInfo.IME_ACTION_DONE }
          .map { EnterOtpSubmitted(otpEntryEditText.text.toString()) }

  private fun resendSmsClicks() =
      RxView.clicks(resendSmsButton).map { EnterOtpResendSmsClicked() }

  private fun otpTextChanges() =
      RxTextView.textChanges(otpEntryEditText).map { EnterOtpTextChanges(it.toString()) }

  fun showUserPhoneNumber(phoneNumber: String) {
    val phoneNumberWithCountryCode = resources.getString(
        R.string.enterotp_phonenumber,
        resources.getString(R.string.country_dialing_code),
        phoneNumber
    )

    userPhoneNumberTextView.text = phoneNumberWithCountryCode
  }

  fun goBack() {
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
}
