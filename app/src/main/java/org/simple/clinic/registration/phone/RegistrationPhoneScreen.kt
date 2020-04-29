package org.simple.clinic.registration.phone

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_registration_phone.view.*
import org.simple.clinic.R
import org.simple.clinic.appconfig.Country
import org.simple.clinic.bindUiToController
import org.simple.clinic.deniedaccess.AccessDeniedScreenKey
import org.simple.clinic.di.injector
import org.simple.clinic.login.pin.LoginPinScreenKey
import org.simple.clinic.registration.name.RegistrationNameScreenKey
import org.simple.clinic.registration.phone.loggedout.LoggedOutOfDeviceDialog
import org.simple.clinic.router.screen.RouterDirection
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

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var country: Country

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<Injector>().inject(this)

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
      phoneNumberEditText
          .textChanges()
          .map(CharSequence::toString)
          .map(::RegistrationPhoneNumberTextChanged)

  private fun doneClicks() =
      phoneNumberEditText
          .editorActions { it == EditorInfo.IME_ACTION_DONE }
          .map { RegistrationPhoneDoneClicked() }

  fun preFillUserDetails(ongoingEntry: OngoingRegistrationEntry) {
    isdCodeEditText.setText(resources.getString(R.string.registrationphone_country_code, country.isdCode))
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

  fun showLoggedOutOfDeviceDialog() {
    LoggedOutOfDeviceDialog.show(activity.supportFragmentManager)
  }

  fun showAccessDeniedScreen(number: String) {
    screenRouter.clearHistoryAndPush(AccessDeniedScreenKey(number), RouterDirection.REPLACE)
  }

  interface Injector {
    fun inject(target: RegistrationPhoneScreen)
  }
}
