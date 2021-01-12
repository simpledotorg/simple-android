package org.simple.clinic.registration.phone

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.databinding.ScreenRegistrationPhoneBinding
import org.simple.clinic.deniedaccess.AccessDeniedScreenKey
import org.simple.clinic.di.injector
import org.simple.clinic.login.pin.LoginPinScreenKey
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.registration.name.RegistrationNameScreenKey
import org.simple.clinic.registration.phone.loggedout.LoggedOutOfDeviceDialog
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.uuid.UuidGenerator
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class RegistrationPhoneScreen :
    BaseScreen<
        RegistrationPhoneScreenKey,
        ScreenRegistrationPhoneBinding,
        RegistrationPhoneModel,
        RegistrationPhoneEvent,
        RegistrationPhoneEffect,
        RegistrationPhoneUiRenderer>(),
    RegistrationPhoneUi,
    RegistrationPhoneUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var country: Country

  @Inject
  lateinit var effectHandlerFactory: RegistrationPhoneEffectHandler.Factory

  private val isdCodeEditText
    get() = binding.isdCodeEditText

  private val phoneNumberEditText
    get() = binding.phoneNumberEditText

  private val validationErrorTextView
    get() = binding.validationErrorTextView

  private val progressView
    get() = binding.progressView

  @Inject
  lateinit var uuidGenerator: UuidGenerator

  override fun defaultModel() = RegistrationPhoneModel.create(OngoingRegistrationEntry(uuid = uuidGenerator.v4()))

  override fun uiRenderer() = RegistrationPhoneUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenRegistrationPhoneBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .merge(
          phoneNumberTextChanges(),
          doneClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<RegistrationPhoneEvent>()

  override fun createUpdate() = RegistrationPhoneUpdate()

  override fun createInit() = RegistrationPhoneInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    isdCodeEditText.setText(resources.getString(R.string.registrationphone_country_code, country.isdCode))
    phoneNumberEditText.showKeyboard()
  }

  private fun phoneNumberTextChanges() =
      phoneNumberEditText
          .textChanges()
          .map(CharSequence::toString)
          .map(::RegistrationPhoneNumberTextChanged)

  private fun doneClicks() =
      phoneNumberEditText
          .editorActions { it == EditorInfo.IME_ACTION_DONE }
          .map { RegistrationPhoneDoneClicked() }

  override fun preFillUserDetails(ongoingEntry: OngoingRegistrationEntry) {
    phoneNumberEditText.setTextAndCursor(ongoingEntry.phoneNumber)
  }

  override fun openRegistrationNameEntryScreen(currentRegistrationEntry: OngoingRegistrationEntry) {
    router.push(RegistrationNameScreenKey(currentRegistrationEntry))
  }

  override fun showInvalidNumberError() {
    showError(R.string.registrationphone_error_invalid_number)
  }

  override fun showUnexpectedErrorMessage() {
    showError(R.string.registrationphone_error_unexpected_error)
  }

  override fun showNetworkErrorMessage() {
    showError(R.string.registrationphone_error_check_internet_connection)
  }

  private fun showError(@StringRes errorResId: Int) {
    validationErrorTextView.visibility = View.VISIBLE
    validationErrorTextView.text = resources.getString(errorResId)
  }

  override fun hideAnyError() {
    validationErrorTextView.visibility = View.GONE
  }

  override fun showProgressIndicator() {
    progressView.visibility = View.VISIBLE
  }

  override fun hideProgressIndicator() {
    progressView.visibility = View.GONE
  }

  override fun openLoginPinEntryScreen() {
    router.push(LoginPinScreenKey())
  }

  override fun showLoggedOutOfDeviceDialog() {
    LoggedOutOfDeviceDialog.show(activity.supportFragmentManager)
  }

  override fun showAccessDeniedScreen(number: String) {
    router.clearHistoryAndPush(AccessDeniedScreenKey(number).wrap())
  }

  interface Injector {
    fun inject(target: RegistrationPhoneScreen)
  }
}
