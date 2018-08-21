package org.simple.clinic.login.pin

import android.content.Context
import android.support.transition.TransitionManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class LoginPinScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: LoginPinScreenController

  private val rootLayout by bindView<ViewGroup>(R.id.loginpin_root)
  private val phoneNumberTextView by bindView<TextView>(R.id.loginpin_phone_number)
  private val pinEditText by bindView<EditText>(R.id.loginpin_pin)
  private val progressBar by bindView<ProgressBar>(R.id.loginpin_progress)
  private val errorTextView by bindView<TextView>(R.id.loginpin_error)
  private val backButton by bindView<ImageButton>(R.id.loginpin_back)
  private val loginFormLayout by bindView<LinearLayout>(R.id.loginpin_form)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    Observable.mergeArray(screenCreates(), pinTextChanges(), submitClicks(), backClicks(), otpReceived())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates(): Observable<UiEvent> {
    return Observable.just(PinScreenCreated())
  }

  private fun pinTextChanges() =
      RxTextView.textChanges(pinEditText)
          .map(CharSequence::toString)
          .map(::PinTextChanged)

  private fun submitClicks() =
      RxTextView.editorActions(pinEditText) { it == EditorInfo.IME_ACTION_DONE }
          .map { PinSubmitClicked() }

  private fun backClicks() =
      RxView.clicks(backButton)
          .map { PinBackClicked() }

  private fun otpReceived(): Observable<LoginPinOtpReceived>? {
    val key = screenRouter.key<LoginPinScreenKey>(this)
    return Single.just(LoginPinOtpReceived(key.otp)).toObservable()
  }

  fun showPhoneNumber(phoneNumber: String) {
    phoneNumberTextView.text = phoneNumber
  }

  fun showProgressBar() {
    TransitionManager.beginDelayedTransition(this)
    rootLayout.hideKeyboard()

    progressBar.visibility = View.VISIBLE
    loginFormLayout.visibility = View.INVISIBLE
  }

  fun hideProgressBar() {
    TransitionManager.beginDelayedTransition(this)

    progressBar.visibility = View.INVISIBLE
    loginFormLayout.visibility = View.VISIBLE
  }

  fun showNetworkError() {
    errorTextView.text = context.getString(R.string.loginpin_error_check_internet_connection)
    errorTextView.visibility = View.VISIBLE
  }

  fun showServerError(errorToShow: String) {
    errorTextView.text = errorToShow
    errorTextView.visibility = View.VISIBLE
  }

  fun showUnexpectedError() {
    errorTextView.text = context.getString(R.string.api_unexpected_error)
    errorTextView.visibility = View.VISIBLE
  }

  fun openHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreen.KEY, RouterDirection.REPLACE)
  }

  fun goBackToLoginPhoneScreen() {
    screenRouter.pop()
  }
}
