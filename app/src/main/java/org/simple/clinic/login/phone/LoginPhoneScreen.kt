package org.simple.clinic.login.phone

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.login.pin.LoginPinScreen
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class LoginPhoneScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY: (otp: String) -> LoginPhoneScreenKey = ::LoginPhoneScreenKey
  }

  @Inject
  lateinit var controller: LoginPhoneScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val phoneNumberEditText by bindView<EditText>(R.id.loginphone_phone)
  private val submitButton by bindView<Button>(R.id.loginphone_submit)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.mergeArray(screenCreates(), phoneNumberTextChanges(), nextClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<LoginPhoneScreenKey>(this)!!
    return Observable.just(LoginPhoneNumberScreenCreated(screenKey.otp))
  }

  private fun phoneNumberTextChanges() = RxTextView.textChanges(phoneNumberEditText)
      .map(CharSequence::toString)
      .map(::LoginPhoneNumberTextChanged)

  private fun nextClicks(): Observable<UiEvent> {
    val phoneImeClicks = RxTextView.editorActions(phoneNumberEditText) { it == EditorInfo.IME_ACTION_DONE }

    return RxView.clicks(submitButton)
        .mergeWith(phoneImeClicks)
        .map { LoginPhoneNumberSubmitClicked() }
  }

  fun enableSubmitButton(enabled: Boolean) {
    submitButton.isEnabled = enabled
  }

  fun openLoginPinScreen() {
    screenRouter.push(LoginPinScreen.KEY)
  }
}
