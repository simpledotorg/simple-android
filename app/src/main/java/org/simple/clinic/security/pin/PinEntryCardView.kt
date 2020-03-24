package org.simple.clinic.security.pin

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.pin_entry_card.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.setPaddingBottom
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class PinEntryCardView(context: Context, attrs: AttributeSet) : CardView(context, attrs), PinEntryUi, UiActions {

  @Inject
  lateinit var controller: PinEntryCardController

  @Inject
  lateinit var effectHandlerFactory: PinEntryEffectHandler.Factory

  val upstreamUiEvents: PublishSubject<UiEvent> = PublishSubject.create<UiEvent>()
  val downstreamUiEvents: PublishSubject<UiEvent> = PublishSubject.create<UiEvent>()

  init {
    LayoutInflater.from(context).inflate(R.layout.pin_entry_card, this, true)
    moveToState(PinEntryUi.State.PinEntry)
    setForgotButtonVisible(true)
  }

  private val events by unsafeLazy {
    Observable
        .merge(
            viewCreated(),
            pinTextChanges(),
            upstreamUiEvents
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val uiRenderer = PinEntryUiRenderer(this)

  private val delegate: MobiusDelegate<PinEntryModel, PinEntryEvent, PinEntryEffect> by unsafeLazy {
    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = PinEntryModel.default(),
        init = PinEntryInit(),
        update = PinEntryUpdate(submitPinAtLength = 4),
        effectHandler = effectHandlerFactory.create(uiActions = this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun viewCreated() = Observable.just(PinEntryViewCreated)

  private fun pinTextChanges() =
      pinEditText.textChanges()
          .map(CharSequence::toString)
          .map(::PinTextChanged)

  override fun moveToState(state: PinEntryUi.State) {
    val transition = AutoTransition()
        .setOrdering(AutoTransition.ORDERING_TOGETHER)
        .setDuration(200)
        .setInterpolator(FastOutSlowInInterpolator())
        .removeTarget(errorTextView)
    TransitionManager.beginDelayedTransition(this, transition)

    contentContainer.visibility = when (state) {
      is PinEntryUi.State.PinEntry -> VISIBLE
      is PinEntryUi.State.BruteForceLocked -> VISIBLE
      is PinEntryUi.State.Progress -> INVISIBLE
    }

    progressView.visibility = when (state) {
      is PinEntryUi.State.PinEntry -> GONE
      is PinEntryUi.State.BruteForceLocked -> GONE
      is PinEntryUi.State.Progress -> VISIBLE
    }

    pinAndLockViewFlipper.displayedChildResId = when (state) {
      is PinEntryUi.State.PinEntry -> R.id.pinEditText
      is PinEntryUi.State.BruteForceLocked -> R.id.pinentry_bruteforcelock
      is PinEntryUi.State.Progress -> pinAndLockViewFlipper.displayedChildResId
    }

    when (state) {
      is PinEntryUi.State.PinEntry -> pinEditText.showKeyboard()
      is PinEntryUi.State.Progress -> hideKeyboard()
      is PinEntryUi.State.BruteForceLocked -> hideKeyboard()
    }.exhaustive()

    if (state is PinEntryUi.State.BruteForceLocked) {
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

  override fun hideError() {
    errorTextView.visibility = View.GONE
  }

  override fun showIncorrectPinErrorForFirstAttempt() {
    showError(resources.getString(R.string.pinentry_error_incorrect_pin_on_first_attempt))
  }

  @SuppressLint("StringFormatMatches")
  override fun showIncorrectPinErrorOnSubsequentAttempts(remaining: Int) {
    showError(resources.getString(R.string.pinentry_error_incorrect_pin_attempts_remaining, remaining.toString()))
  }

  @SuppressLint("StringFormatMatches")
  override fun showIncorrectAttemptsLimitReachedError(attemptsMade: Int) {
    showError(resources.getString(R.string.pinentry_error_incorrect_pin_attempts_limit_reached, attemptsMade.toString()))
  }

  override fun clearPin() {
    pinEditText.text = null
  }

  override fun dispatchAuthenticatedCallback(enteredPin: String) {
    downstreamUiEvents.onNext(PinAuthenticated(enteredPin))
  }

  /** Defaults to visible. */
  override fun setForgotButtonVisible(visible: Boolean) {
    if (visible) {
      forgotPinButton.visibility = View.VISIBLE
      contentContainer.setPaddingBottom(R.dimen.pinentry_content_bottom_spacing_with_forgot_pin)

    } else {
      forgotPinButton.visibility = View.GONE
      contentContainer.setPaddingBottom(R.dimen.pinentry_content_bottom_spacing_without_forgot_pin)
    }
  }
}

