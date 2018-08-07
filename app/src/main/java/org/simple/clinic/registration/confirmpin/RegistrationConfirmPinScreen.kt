package org.simple.clinic.registration.confirmpin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
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
import org.simple.clinic.registration.location.RegistrationLocationPermissionScreen
import org.simple.clinic.registration.pin.RegistrationPinScreen
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class RegistrationConfirmPinScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationConfirmPinScreenController

  private val backButton by bindView<ImageButton>(R.id.registrationconfirmpin_back)
  private val fullNameTextView by bindView<TextView>(R.id.registrationconfirmpin_user_fullname)
  private val phoneNumberTextView by bindView<TextView>(R.id.registrationconfirmpin_user_phone)
  private val confirmPinEditText by bindView<EditText>(R.id.registrationconfirmpin_pin)
  private val pinHintTextView by bindView<TextView>(R.id.registrationconfirmpin_pin_hint)
  private val errorStateViewGroup by bindView<LinearLayout>(R.id.registrationconfirmpin_error)
  private val resetPinButton by bindView<Button>(R.id.registrationconfirmpin_reset_pin)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    backButton.setOnClickListener {
      screenRouter.pop()
    }

    Observable.merge(screenCreates(), confirmPinTextChanges(), resetPinClicks(), doneClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    // Showing the keyboard again in case the user returns from location permission screen.
    confirmPinEditText.showKeyboard()
  }

  private fun screenCreates() = Observable.just(RegistrationConfirmPinScreenCreated())

  private fun confirmPinTextChanges() =
      RxTextView.textChanges(confirmPinEditText)
          .map(CharSequence::toString)
          .map(::RegistrationConfirmPinTextChanged)

  private fun resetPinClicks() =
      RxView.clicks(resetPinButton)
          .map { RegistrationResetPinClicked() }

  private fun doneClicks() =
      RxTextView
          .editorActions(confirmPinEditText) { it == EditorInfo.IME_ACTION_DONE }
          .map { RegistrationConfirmPinDoneClicked() }

  fun showPinMismatchError() {
    errorStateViewGroup.visibility = View.VISIBLE
    pinHintTextView.visibility = View.GONE
  }

  fun hidePinMismatchError() {
    errorStateViewGroup.visibility = View.GONE
    pinHintTextView.visibility = View.VISIBLE
  }

  fun openFacilitySelectionScreen() {
    screenRouter.push(RegistrationLocationPermissionScreen.KEY)
  }

  fun preFillUserDetails(ongoingEntry: OngoingRegistrationEntry) {
    fullNameTextView.text = ongoingEntry.fullName
    phoneNumberTextView.text = ongoingEntry.phoneNumber
    confirmPinEditText.setTextAndCursor(ongoingEntry.pinConfirmation)
  }

  fun goBackToPinScreen() {
    screenRouter.push(RegistrationPinScreen.KEY)
  }

  companion object {
    val KEY = RegistrationConfirmPinScreenKey()
  }
}
