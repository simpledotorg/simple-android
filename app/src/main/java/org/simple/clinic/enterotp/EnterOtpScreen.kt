package org.simple.clinic.enterotp

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.LOGIN_OTP_LENGTH
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.databinding.ScreenEnterotpBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class EnterOtpScreen(
    context: Context,
    attributeSet: AttributeSet
) : RelativeLayout(context, attributeSet), EnterOtpUi, EnterOtpUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var country: Country

  @Inject
  lateinit var effectHandlerFactory: EnterOtpEffectHandler.Factory

  private var binding: ScreenEnterotpBinding? = null

  private val otpEntryEditText
    get() = binding!!.otpEntryEditText

  private val backButton
    get() = binding!!.backButton

  private val resendSmsButton
    get() = binding!!.resendSmsButton

  private val userPhoneNumberTextView
    get() = binding!!.userPhoneNumberTextView

  private val smsSentTextView
    get() = binding!!.smsSentTextView

  private val errorTextView
    get() = binding!!.errorTextView

  private val validateOtpProgressBar
    get() = binding!!.validateOtpProgressBar

  private val otpEntryContainer
    get() = binding!!.otpEntryContainer

  private val events by unsafeLazy {
    Observable
        .mergeArray(
            otpSubmits(),
            resendSmsClicks()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = EnterOtpUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = EnterOtpModel.create(),
        update = EnterOtpUpdate(LOGIN_OTP_LENGTH),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = EnterOtpInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenEnterotpBinding.bind(this)

    context.injector<Injector>().inject(this)

    otpEntryEditText.showKeyboard()
    backButton.setOnClickListener { goBack() }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun otpSubmits(): Observable<UiEvent> {
    val otpFromImeClicks: Observable<UiEvent> = otpEntryEditText
        .editorActions() { it == EditorInfo.IME_ACTION_DONE }
        .map { EnterOtpSubmitted(otpEntryEditText.text.toString()) }

    val otpFromTextChanges: Observable<UiEvent> = otpEntryEditText
        .textChanges()
        .filter { it.length == LOGIN_OTP_LENGTH }
        .map { EnterOtpSubmitted(it.toString()) }

    return otpFromImeClicks.mergeWith(otpFromTextChanges)
  }

  private fun resendSmsClicks() =
      resendSmsButton.clicks().map { EnterOtpResendSmsClicked() }

  override fun showUserPhoneNumber(phoneNumber: String) {
    val phoneNumberWithCountryCode = resources.getString(
        R.string.enterotp_phonenumber,
        country.isdCode,
        phoneNumber
    )

    userPhoneNumberTextView.text = phoneNumberWithCountryCode
  }

  override fun goBack() {
    hideKeyboard()
    router.pop()
  }

  override fun showUnexpectedError() {
    showError(resources.getString(R.string.api_unexpected_error))
  }

  override fun showNetworkError() {
    showError(resources.getString(R.string.api_network_error))
  }

  override fun showServerError(error: String) {
    showError(error)
    otpEntryEditText.showKeyboard()
  }

  override fun showIncorrectOtpError() {
    showError(resources.getString(R.string.enterotp_incorrect_code))
    otpEntryEditText.showKeyboard()
  }

  private fun showError(error: String) {
    smsSentTextView.visibility = View.GONE
    errorTextView.text = error
    errorTextView.visibility = View.VISIBLE
  }

  override fun hideError() {
    errorTextView.visibility = View.GONE
  }

  override fun showProgress() {
    TransitionManager.beginDelayedTransition(this)
    validateOtpProgressBar.visibility = View.VISIBLE
    otpEntryContainer.visibility = View.INVISIBLE
  }

  override fun hideProgress() {
    TransitionManager.beginDelayedTransition(this)
    validateOtpProgressBar.visibility = View.INVISIBLE
    otpEntryContainer.visibility = View.VISIBLE
  }

  override fun showSmsSentMessage() {
    smsSentTextView.visibility = View.VISIBLE
  }

  override fun clearPin() {
    otpEntryEditText.text = null
  }

  interface Injector {
    fun inject(target: EnterOtpScreen)
  }
}
