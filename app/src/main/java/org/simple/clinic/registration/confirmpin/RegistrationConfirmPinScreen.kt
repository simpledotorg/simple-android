package org.simple.clinic.registration.confirmpin

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.widgets.setTextAndCursor
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
  private val nextButton by bindView<Button>(R.id.registrationconfirmpin_next)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    backButton.setOnClickListener {
      screenRouter.pop()
    }

    Observable.merge(screenCreates(), confirmPinTextChanges(), nextClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(RegistrationConfirmPinScreenCreated())

  private fun confirmPinTextChanges() =
      RxTextView.textChanges(confirmPinEditText)
          .map(CharSequence::toString)
          .map(::RegistrationConfirmPinTextChanged)

  private fun nextClicks() =
      RxView.clicks(nextButton)
          .map { RegistrationConfirmPinNextClicked() }

  fun openFacilitySelectionScreen() {
    // TODO: Open facility selection instead.
    screenRouter.clearHistoryAndPush(HomeScreen.KEY, RouterDirection.FORWARD)
  }

  fun setNextButtonEnabled(enabled: Boolean) {
    nextButton.isEnabled = enabled
  }

  fun preFillUserDetails(ongoingEntry: OngoingRegistrationEntry) {
    fullNameTextView.text = ongoingEntry.fullName
    phoneNumberTextView.text = ongoingEntry.phoneNumber
    confirmPinEditText.setTextAndCursor(ongoingEntry.pinConfirmation)
  }

  companion object {
    val KEY = RegistrationConfirmPinScreenKey()
  }
}
