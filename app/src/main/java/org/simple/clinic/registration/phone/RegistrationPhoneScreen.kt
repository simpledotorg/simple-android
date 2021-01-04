package org.simple.clinic.registration.phone

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.databinding.ScreenRegistrationPhoneBinding
import org.simple.clinic.deniedaccess.AccessDeniedScreenKey
import org.simple.clinic.di.injector
import org.simple.clinic.login.pin.LoginPinScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.registration.name.RegistrationNameScreenKey
import org.simple.clinic.registration.phone.loggedout.LoggedOutOfDeviceDialog
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.uuid.UuidGenerator
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class RegistrationPhoneScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), RegistrationPhoneUi, RegistrationPhoneUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var country: Country

  @Inject
  lateinit var effectHandlerFactory: RegistrationPhoneEffectHandler.Factory

  var binding: ScreenRegistrationPhoneBinding? = null

  private val isdCodeEditText
    get() = binding!!.isdCodeEditText

  private val phoneNumberEditText
    get() = binding!!.phoneNumberEditText

  private val validationErrorTextView
    get() = binding!!.validationErrorTextView

  private val progressView
    get() = binding!!.progressView

  @Inject
  lateinit var uuidGenerator: UuidGenerator

  private val events by unsafeLazy {
    Observable
        .merge(
            phoneNumberTextChanges(),
            doneClicks()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val uiRenderer = RegistrationPhoneUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = RegistrationPhoneModel.create(OngoingRegistrationEntry(uuid = uuidGenerator.v4())),
        update = RegistrationPhoneUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = RegistrationPhoneInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    binding = ScreenRegistrationPhoneBinding.bind(this)
    if (isInEditMode) {
      return
    }
    context.injector<Injector>().inject(this)

    isdCodeEditText.setText(resources.getString(R.string.registrationphone_country_code, country.isdCode))
    phoneNumberEditText.showKeyboard()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
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
    router.push(RegistrationNameScreenKey(currentRegistrationEntry).wrap())
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
    progressView.visibility = VISIBLE
  }

  override fun hideProgressIndicator() {
    progressView.visibility = GONE
  }

  override fun openLoginPinEntryScreen() {
    router.push(LoginPinScreenKey().wrap())
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
