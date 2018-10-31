package org.simple.clinic.pin

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.StringRes
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.StaggeredEditText
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class PinEntryCardView(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

  @Inject
  lateinit var controller: PinEntryCardController

  val pinEditText by bindView<StaggeredEditText>(R.id.pinentry_pin)
  val forgotPinButton by bindView<Button>(R.id.pinentry_forgotpin)

  private val pinContainer by bindView<ViewGroup>(R.id.pinentry_pin_container)
  private val progressView by bindView<View>(R.id.pinentry_progress)
  private val errorTextView by bindView<TextView>(R.id.pinentry_error)
  private val successfulAuthSubject = PublishSubject.create<PinAuthenticated>()

  sealed class State {
    object PinEntry : State()
    object Progress : State()
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

    Observable.mergeArray(pinTextChanges())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun pinTextChanges() =
      pinEditText.textChanges()
          .map(CharSequence::toString)
          .map(::PinTextChanged)

  fun moveToState(state: State) {
    TransitionManager.beginDelayedTransition(pinContainer, Fade()
        .setDuration(100)
        .setInterpolator(FastOutSlowInInterpolator()))

    when (state) {
      is State.PinEntry -> {
        pinContainer.visibility = View.VISIBLE
        progressView.visibility = View.GONE
        pinEditText.showKeyboard()
      }
      is State.Progress -> {
        pinContainer.visibility = View.INVISIBLE
        progressView.visibility = View.VISIBLE
        pinContainer.hideKeyboard()
      }
    }
  }

  private fun showError(@StringRes errorRes: Int) {
    errorTextView.setText(errorRes)
    errorTextView.visibility = View.VISIBLE
  }

  fun hideError() {
    errorTextView.visibility = View.GONE
  }

  fun showIncorrectPinError() {
    showError(R.string.pinentry_error_incorrect_pin)
  }

  fun clearPin() {
    pinEditText.text = null
  }

  fun dispatchAuthenticatedCallback() {
    successfulAuthSubject.onNext(PinAuthenticated())
  }

  fun successfulAuthentication(): Single<PinAuthenticated> {
    return successfulAuthSubject.firstOrError()
  }
}
