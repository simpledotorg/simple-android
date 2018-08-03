package org.simple.clinic.registration.phone

import android.content.Context
import android.support.transition.ChangeBounds
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.registration.name.RegistrationFullNameScreen
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class RegistrationPhoneScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationPhoneScreenController

  private val contentCardView by bindView<CardView>(R.id.registrationphone_card)
  private val phoneNumberEditText by bindView<EditText>(R.id.registrationphone_phone)
  private val validationErrorTextView by bindView<TextView>(R.id.registrationphone_error)

  private val contentChangeTransitions = TransitionSet()
      .addTransition(Fade())
      .addTransition(ChangeBounds())
      .setOrdering(TransitionSet.ORDERING_TOGETHER)
      .setDuration(200)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    phoneNumberEditText.showKeyboard()

    Observable.merge(screenCreates(), phoneNumberTextChanges(), doneClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(RegistrationPhoneScreenCreated())

  private fun phoneNumberTextChanges() =
      RxTextView.textChanges(phoneNumberEditText)
          .map(CharSequence::toString)
          .map(::RegistrationPhoneNumberTextChanged)

  private fun doneClicks() =
      RxTextView
          .editorActions(phoneNumberEditText) { it == EditorInfo.IME_ACTION_DONE }
          .map { RegistrationPhoneDoneClicked() }

  fun preFillUserDetails(ongoingEntry: OngoingRegistrationEntry) {
    phoneNumberEditText.setTextAndCursor(ongoingEntry.phoneNumber)
  }

  fun openRegistrationNameEntryScreen() {
    screenRouter.push(RegistrationFullNameScreen.KEY)
  }

  fun showInvalidNumberError() {
    TransitionManager.beginDelayedTransition(contentCardView, contentChangeTransitions)
    validationErrorTextView.visibility = View.VISIBLE
    validationErrorTextView.text = resources.getString(R.string.registrationphone_error_invalid_number)
  }

  fun hideInvalidNumberError() {
    TransitionManager.beginDelayedTransition(contentCardView, contentChangeTransitions)
    validationErrorTextView.visibility = View.GONE
  }

  companion object {
    val KEY = RegistrationPhoneScreenKey()
  }
}
