package org.simple.clinic.enterotp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.LOGIN_OTP_LENGTH
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.databinding.ScreenEnterotpBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class EnterOtpScreen : BaseScreen<
    EnterOtpScreen.Key,
    ScreenEnterotpBinding,
    EnterOtpModel,
    EnterOtpEvent,
    EnterOtpEffect,
    Unit>(), EnterOtpUi, EnterOtpUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var country: Country

  @Inject
  lateinit var effectHandlerFactory: EnterOtpEffectHandler.Factory

  private val otpEntryEditText
    get() = binding.otpEntryEditText

  private val backButton
    get() = binding.backButton

  private val resendSmsButton
    get() = binding.resendSmsButton

  private val userPhoneNumberTextView
    get() = binding.userPhoneNumberTextView

  private val smsSentTextView
    get() = binding.smsSentTextView

  private val errorTextView
    get() = binding.errorTextView

  private val validateOtpProgressBar
    get() = binding.validateOtpProgressBar

  private val otpEntryContainer
    get() = binding.otpEntryContainer

  private val rootLayout
    get() = binding.rootLayout

  override fun defaultModel() = EnterOtpModel.create()

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenEnterotpBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .mergeArray(
          otpSubmits(),
          resendSmsClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<EnterOtpEvent>()

  override fun createUpdate() = EnterOtpUpdate(LOGIN_OTP_LENGTH)

  override fun createEffectHandler(
      viewEffectsConsumer: Consumer<Unit>
  ) = effectHandlerFactory.create(this).build()

  override fun createInit() = EnterOtpInit()

  override fun uiRenderer() = EnterOtpUiRenderer(this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    otpEntryEditText.showKeyboard()
    backButton.setOnClickListener { goBack() }
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
    rootLayout.hideKeyboard()
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
    TransitionManager.beginDelayedTransition(rootLayout)
    validateOtpProgressBar.visibility = View.VISIBLE
    otpEntryContainer.visibility = View.INVISIBLE
  }

  override fun hideProgress() {
    TransitionManager.beginDelayedTransition(rootLayout)
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

  @Parcelize
  data class Key(
      override val analyticsName: String = "Enter Login OTP Manually"
  ) : ScreenKey() {
    override fun instantiateFragment() = EnterOtpScreen()
  }
}
