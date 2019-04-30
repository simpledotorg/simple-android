package org.simple.clinic.registration.phone

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.login.pin.LoginPinScreenKey
import org.simple.clinic.registration.name.RegistrationNameScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class RegistrationPhoneScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationPhoneScreenController

  private val phoneNumberEditText by bindView<EditText>(R.id.registrationphone_phone)
  private val validationErrorTextView by bindView<TextView>(R.id.registrationphone_error)
  private val progressView by bindView<View>(R.id.registrationphone_progress)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    phoneNumberEditText.showKeyboard()

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            phoneNumberTextChanges(),
            doneClicks()
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
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
    screenRouter.push(RegistrationNameScreenKey())
  }

  fun showInvalidNumberError() {
    showError(R.string.registrationphone_error_invalid_number)
  }

  fun showUnexpectedErrorMessage() {
    showError(R.string.registrationphone_error_unexpected_error)
  }

  fun showNetworkErrorMessage() {
    showError(R.string.registrationphone_error_check_internet_connection)
  }

  private fun showError(@StringRes errorResId: Int) {
    validationErrorTextView.visibility = View.VISIBLE
    validationErrorTextView.text = resources.getString(errorResId)
  }

  fun hideAnyError() {
    validationErrorTextView.visibility = View.GONE
  }

  fun showProgressIndicator() {
    progressView.visibility = VISIBLE
  }

  fun hideProgressIndicator() {
    progressView.visibility = GONE
  }

  fun openLoginPinEntryScreen() {
    screenRouter.push(LoginPinScreenKey())
  }
}
