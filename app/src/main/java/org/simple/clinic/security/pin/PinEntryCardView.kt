package org.simple.clinic.security.pin

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.StringRes
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ViewFlipper
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.StaggeredEditText
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import timber.log.Timber
import javax.inject.Inject

class PinEntryCardView(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

  @Inject
  lateinit var controller: PinEntryCardController

  val pinEditText by bindView<StaggeredEditText>(R.id.pinentry_pin)
  val forgotPinButton by bindView<Button>(R.id.pinentry_forgotpin)

  private val progressView by bindView<View>(R.id.pinentry_progress)
  private val contentContainer by bindView<ViewGroup>(R.id.pinentry_content_container)
  private val pinAndLockViewFlipper by bindView<ViewFlipper>(R.id.pinentry_pin_and_bruteforcelock_viewflipper)
  private val timeRemainingTillUnlockTextView by bindView<TextView>(R.id.pinentry_bruteforcelock_time_remaining)
  private val errorTextView by bindView<TextView>(R.id.pinentry_error)
  private val successfulAuthSubject = PublishSubject.create<PinAuthenticated>()

  sealed class State {
    object PinEntry : State()
    object Progress : State()
    data class BruteForceLocked(val timeTillUnlock: String) : State()
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.pin_entry_card, this, true)
    moveToState(State.PinEntry)
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.mergeArray(viewCreated(), pinTextChanges())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun viewCreated() = Observable.just(PinEntryViewCreated)

  private fun pinTextChanges() =
      pinEditText.textChanges()
          .map(CharSequence::toString)
          .map(::PinTextChanged)

  fun moveToState(state: State) {
    contentContainer.visibility = when (state) {
      is State.PinEntry -> View.VISIBLE
      is State.BruteForceLocked -> View.VISIBLE
      is State.Progress -> View.INVISIBLE
    }

    progressView.visibility = when (state) {
      is State.PinEntry -> View.GONE
      is State.BruteForceLocked -> View.GONE
      is State.Progress -> View.VISIBLE
    }

    // TODO: Use IDs instead of indices.
    pinAndLockViewFlipper.displayedChild = when (state) {
      State.PinEntry -> 0
      State.Progress -> pinAndLockViewFlipper.displayedChild
      is State.BruteForceLocked -> 1
    }

    when (state) {
      is State.PinEntry -> pinEditText.showKeyboard()
      is State.Progress -> hideKeyboard()
      is State.BruteForceLocked -> hideKeyboard()
    }.exhaustive()

    if (state is State.BruteForceLocked) {
      timeRemainingTillUnlockTextView.text = state.timeTillUnlock
    }
  }

  private fun showError(@StringRes errorRes: Int) {
    Timber.i("Showing error")
    errorTextView.setText(errorRes)
    errorTextView.visibility = View.VISIBLE
  }

  fun hideError() {
    Timber.i("Hiding error")
    errorTextView.visibility = View.GONE
  }

  fun showIncorrectPinError() {
    showError(R.string.pinentry_error_incorrect_pin)
  }

  fun clearPin() {
    Timber.i("Clearing PIN")
    pinEditText.text = null
  }

  fun dispatchAuthenticatedCallback() {
    successfulAuthSubject.onNext(PinAuthenticated())
  }

  fun successfulAuthentication(): Single<PinAuthenticated> {
    return successfulAuthSubject.firstOrError()
  }
}
