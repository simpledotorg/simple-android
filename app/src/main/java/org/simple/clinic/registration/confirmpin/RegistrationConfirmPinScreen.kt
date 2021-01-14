package org.simple.clinic.registration.confirmpin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.SECURITY_PIN_LENGTH
import org.simple.clinic.databinding.ScreenRegistrationConfirmPinBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.registration.location.RegistrationLocationPermissionScreenKey
import org.simple.clinic.registration.pin.RegistrationPinScreenKey
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class RegistrationConfirmPinScreen :
    BaseScreen<
        RegistrationConfirmPinScreenKey,
        ScreenRegistrationConfirmPinBinding,
        RegistrationConfirmPinModel,
        RegistrationConfirmPinEvent,
        RegistrationConfirmPinEffect,
        RegistrationConfirmPinUiRenderer>(),
    RegistrationConfirmPinUi,
    RegistrationConfirmPinUiActions {

  private val confirmPinEditText
    get() = binding.confirmPinEditText

  private val backButton
    get() = binding.backButton

  private val resetPinButton
    get() = binding.resetPinButton

  private val errorStateViewGroup
    get() = binding.errorStateViewGroup

  private val pinHintTextView
    get() = binding.pinHintTextView

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: RegistrationConfirmPinEffectHandler.Factory

  override fun defaultModel() = RegistrationConfirmPinModel.create(screenKey.registrationEntry)

  override fun uiRenderer() = RegistrationConfirmPinUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenRegistrationConfirmPinBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .merge(
          confirmPinTextChanges(),
          resetPinClicks(),
          doneClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<RegistrationConfirmPinEvent>()

  override fun createUpdate() = RegistrationConfirmPinUpdate()

  override fun createInit() = RegistrationConfirmPinInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    backButton.setOnClickListener {
      router.pop()
    }

    // Showing the keyboard again in case the user returns from location permission screen.
    confirmPinEditText.showKeyboard()
  }

  private fun confirmPinTextChanges() =
      confirmPinEditText
          .textChanges()
          .map(CharSequence::toString)
          .map(::RegistrationConfirmPinTextChanged)

  private fun resetPinClicks() =
      resetPinButton
          .clicks()
          .map { RegistrationResetPinClicked() }

  private fun doneClicks(): Observable<RegistrationConfirmPinDoneClicked>? {
    val imeDoneClicks = confirmPinEditText
        .editorActions() { it == EditorInfo.IME_ACTION_DONE }
        .map { RegistrationConfirmPinDoneClicked() }

    val pinAutoSubmits = confirmPinEditText
        .textChanges()
        // Because PIN is auto-submitted when 4 digits are entered, restoring the
        // existing PIN will immediately take the user to the next screen.
        .skip(1)
        .filter { it.length == SECURITY_PIN_LENGTH }
        .map { RegistrationConfirmPinDoneClicked() }

    return imeDoneClicks.mergeWith(pinAutoSubmits)
  }

  override fun showPinMismatchError() {
    errorStateViewGroup.visibility = View.VISIBLE
    pinHintTextView.visibility = View.GONE
  }

  override fun clearPin() {
    confirmPinEditText.text = null
  }

  override fun openFacilitySelectionScreen(entry: OngoingRegistrationEntry) {
    binding.root.hideKeyboard()
    router.push(RegistrationLocationPermissionScreenKey(entry).wrap())
  }

  override fun goBackToPinScreen(entry: OngoingRegistrationEntry) {
    router.replaceKeyOfSameType(RegistrationPinScreenKey(entry))
  }

  interface Injector {
    fun inject(target: RegistrationConfirmPinScreen)
  }
}
