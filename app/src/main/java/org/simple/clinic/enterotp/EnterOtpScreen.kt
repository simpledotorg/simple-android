package org.simple.clinic.enterotp

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.AutoTransition
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
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class EnterOtpScreen : BaseScreen<
    EnterOtpScreen.Key,
    ScreenEnterotpBinding,
    EnterOtpModel,
    EnterOtpEvent,
    EnterOtpEffect,
    EnterOtpViewEffect>(), EnterOtpUi, EnterOtpUiActions {

  companion object {
    private const val MILLIS_IN_SECOND = 1000L
    private const val SECONDS_IN_HOUR = 3600L
    private const val SECONDS_IN_MINUTE = 60L
  }

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var country: Country

  @Inject
  lateinit var effectHandlerFactory: EnterOtpEffectHandler.Factory

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var config: BruteForceOtpEntryProtectionConfig

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

  private val otpEntryAndLockViewFlipper
    get() = binding.OtpEntryAndLockViewFlipper

  private val timeRemainingTillUnlockTextView
    get() = binding.timeRemainingTillUnlockTextView

  private var otpEntryLockedCountdown: CountDownTimer? = null
    set(value) {
      field?.cancel()
      field = value
    }

  override fun defaultModel() = EnterOtpModel.create(
      minOtpRetries = config.minOtpEntries,
      maxOtpEntriesAllowed = config.limitOfFailedAttempts)

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
      viewEffectsConsumer: Consumer<EnterOtpViewEffect>
  ) = effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun viewEffectHandler() = EnterOtpViewEffectHandler(this)

  override fun createInit() = EnterOtpInit()

  override fun uiRenderer() = EnterOtpUiRenderer(this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
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

  override fun showIncorrectOtpError() {
    showError(resources.getString(R.string.enterotp_incorrect_code))
  }

  private fun showError(error: String) {
    smsSentTextView.visibility = View.GONE
    errorTextView.text = error
    errorTextView.visibility = VISIBLE
  }

  override fun hideError() {
    errorTextView.visibility = View.GONE
  }

  override fun showProgress() {
    TransitionManager.beginDelayedTransition(rootLayout)
    validateOtpProgressBar.visibility = VISIBLE
    otpEntryContainer.visibility = View.INVISIBLE
  }

  override fun hideProgress() {
    TransitionManager.beginDelayedTransition(rootLayout)
    validateOtpProgressBar.visibility = View.INVISIBLE
    otpEntryContainer.visibility = VISIBLE
  }

  override fun showSmsSentMessage() {
    smsSentTextView.visibility = VISIBLE
  }

  override fun showOtpEntryMode(mode: OtpEntryMode) {
    val transition = AutoTransition()
        .setOrdering(AutoTransition.ORDERING_TOGETHER)
        .setDuration(200)
        .setInterpolator(FastOutSlowInInterpolator())
        .removeTarget(errorTextView)
    TransitionManager.beginDelayedTransition(rootLayout, transition)

    otpEntryAndLockViewFlipper.displayedChildResId = when (mode) {
      is OtpEntryMode.BruteForceOtpEntryLocked -> R.id.otpentry_bruteforcelock
      OtpEntryMode.OtpEntry -> R.id.otpEntryEditText
    }

    when (mode) {
      is OtpEntryMode.OtpEntry -> otpEntryEditText.showKeyboard()
      is OtpEntryMode.BruteForceOtpEntryLocked -> rootLayout.hideKeyboard()
    }.exhaustive()

    if (mode is OtpEntryMode.BruteForceOtpEntryLocked) {
      val timer = startTimerCountdown(mode.lockUntil)
      timer.start()
      otpEntryLockedCountdown = timer
    }
  }

  override fun showLimitReachedError(attemptsMade: Int) {
    showError(resources.getString(R.string.otpentry_error_incorrect_otp_attempts_limit_reached, attemptsMade.toString()))
    rootLayout.hideKeyboard()
  }

  override fun showResendSmsButton() {
    resendSmsButton.visibility = VISIBLE
  }

  override fun hideResendSmsButton() {
    resendSmsButton.visibility = GONE
  }

  override fun showFailedAttemptOtpError(attemptsRemaining: Int) {
    showError(resources.getString(R.string.otpentry_error_incorrect_otp_attempts_remaining, attemptsRemaining.toString()))
  }

  override fun clearPin() {
    otpEntryEditText.text = null
  }

  private fun startTimerCountdown(until: Instant): CountDownTimer {
    val timeRemaining = Duration.between(Instant.now(utcClock), until).toMillis()

    return object : CountDownTimer(timeRemaining, MILLIS_IN_SECOND) {

      override fun onFinish() {
        /* Nothing to do here */
      }

      override fun onTick(millisUntilFinished: Long) {
        val secondsRemaining = millisUntilFinished / MILLIS_IN_SECOND

        val minutes = (secondsRemaining % SECONDS_IN_HOUR / SECONDS_IN_MINUTE).toString()
        val seconds = (secondsRemaining % SECONDS_IN_MINUTE).toString()

        val minutesWithPadding = minutes.padStart(2, padChar = '0')
        val secondsWithPadding = seconds.padStart(2, padChar = '0')

        timeRemainingTillUnlockTextView.text = resources.getString(
            R.string.otpentry_bruteforcelock_timer,
            minutesWithPadding,
            secondsWithPadding
        )
      }
    }
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
