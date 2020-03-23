package org.simple.clinic.security.pin

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.pin_entry_card.view.*
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.main.TheActivity
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.setPaddingBottom
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class PinEntryCardView(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

  @Inject
  lateinit var controller: PinEntryCardController

  val upstreamUiEvents: PublishSubject<UiEvent> = PublishSubject.create<UiEvent>()
  val downstreamUiEvents: PublishSubject<UiEvent> = PublishSubject.create<UiEvent>()

  sealed class State {
    object PinEntry : State()
    object Progress : State()
    data class BruteForceLocked(val timeTillUnlock: TimerDuration) : State()
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.pin_entry_card, this, true)
    moveToState(State.PinEntry)
    setForgotButtonVisible(true)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            viewCreated(),
            pinTextChanges(),
            upstreamUiEvents
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun viewCreated() = Observable.just(PinEntryViewCreated)

  private fun pinTextChanges() =
      pinEditText.textChanges()
          .map(CharSequence::toString)
          .map(::PinTextChanged)

  fun moveToState(state: State) {
    val transition = AutoTransition()
        .setOrdering(AutoTransition.ORDERING_TOGETHER)
        .setDuration(200)
        .setInterpolator(FastOutSlowInInterpolator())
        .removeTarget(errorTextView)
    TransitionManager.beginDelayedTransition(this, transition)

    contentContainer.visibility = when (state) {
      is State.PinEntry -> VISIBLE
      is State.BruteForceLocked -> VISIBLE
      is State.Progress -> INVISIBLE
    }

    progressView.visibility = when (state) {
      is State.PinEntry -> GONE
      is State.BruteForceLocked -> GONE
      is State.Progress -> VISIBLE
    }

    pinAndLockViewFlipper.displayedChildResId = when (state) {
      is State.PinEntry -> R.id.pinEditText
      is State.BruteForceLocked -> R.id.pinentry_bruteforcelock
      is State.Progress -> pinAndLockViewFlipper.displayedChildResId
    }

    when (state) {
      is State.PinEntry -> pinEditText.showKeyboard()
      is State.Progress -> hideKeyboard()
      is State.BruteForceLocked -> hideKeyboard()
    }.exhaustive()

    if (state is State.BruteForceLocked) {
      timeRemainingTillUnlockTextView.text = resources.getString(
          R.string.pinentry_bruteforcelock_timer,
          state.timeTillUnlock.minutes,
          state.timeTillUnlock.seconds)
    }
  }

  fun showError(error: String) {
    errorTextView.text = error
    errorTextView.visibility = View.VISIBLE
    clearPin()
  }

  fun hideError() {
    errorTextView.visibility = View.GONE
  }

  fun showIncorrectPinErrorForFirstAttempt() {
    showError(resources.getString(R.string.pinentry_error_incorrect_pin_on_first_attempt))
  }

  @SuppressLint("StringFormatMatches")
  fun showIncorrectPinErrorOnSubsequentAttempts(remaining: Int) {
    showError(resources.getString(R.string.pinentry_error_incorrect_pin_attempts_remaining, remaining.toString()))
  }

  @SuppressLint("StringFormatMatches")
  fun showIncorrectAttemptsLimitReachedError(attemptsMade: Int) {
    showError(resources.getString(R.string.pinentry_error_incorrect_pin_attempts_limit_reached, attemptsMade.toString()))
  }

  fun clearPin() {
    pinEditText.text = null
  }

  fun dispatchAuthenticatedCallback(enteredPin: String) {
    downstreamUiEvents.onNext(PinAuthenticated(enteredPin))
  }

  /** Defaults to visible. */
  fun setForgotButtonVisible(visible: Boolean) {
    if (visible) {
      forgotPinButton.visibility = View.VISIBLE
      contentContainer.setPaddingBottom(R.dimen.pinentry_content_bottom_spacing_with_forgot_pin)

    } else {
      forgotPinButton.visibility = View.GONE
      contentContainer.setPaddingBottom(R.dimen.pinentry_content_bottom_spacing_without_forgot_pin)
    }
  }
}

