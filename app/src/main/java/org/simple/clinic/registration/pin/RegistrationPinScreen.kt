package org.simple.clinic.registration.pin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.jakewharton.rxbinding3.widget.editorActions
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.SECURITY_PIN_LENGTH
import org.simple.clinic.databinding.ScreenRegistrationPinBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinScreenKey
import org.simple.clinic.user.OngoingRegistrationEntry
import javax.inject.Inject

class RegistrationPinScreen :
    BaseScreen<
        RegistrationPinScreenKey,
        ScreenRegistrationPinBinding,
        RegistrationPinModel,
        RegistrationPinEvent,
        RegistrationPinEffect,
        RegistrationPinUiRenderer>(),
    RegistrationPinUi,
    RegistrationPinUiActions {

  private val pinEditText
    get() = binding.pinEditText

  private val backButton
    get() = binding.backButton

  private val pinHintTextView
    get() = binding.pinHintTextView

  private val errorTextView
    get() = binding.errorTextView

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: RegistrationPinEffectHandler.Factory

  override fun events() = Observable
      .merge(
          pinTextChanges(),
          doneClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<RegistrationPinEvent>()

  override fun createUpdate() = RegistrationPinUpdate(requiredPinLength = SECURITY_PIN_LENGTH)

  override fun createInit() = RegistrationPinInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun defaultModel() = RegistrationPinModel.create(screenKey.registrationEntry)

  override fun uiRenderer() = RegistrationPinUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenRegistrationPinBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    pinEditText.isSaveEnabled = false

    backButton.setOnClickListener {
      router.pop()
    }

    pinEditText.post { pinEditText.requestFocus() }
  }

  private fun pinTextChanges() =
      pinEditText
          .textChanges()
          .skip(1)
          .map(CharSequence::toString)
          .map(::RegistrationPinTextChanged)

  private fun doneClicks(): Observable<RegistrationPinDoneClicked>? {
    val imeDoneClicks = pinEditText
        .editorActions() { it == EditorInfo.IME_ACTION_DONE }
        .map { RegistrationPinDoneClicked() }

    val pinAutoSubmits = pinEditText
        .textChanges()
        .skip(1)
        .filter { it.length == SECURITY_PIN_LENGTH }
        .map { RegistrationPinDoneClicked() }

    return imeDoneClicks.mergeWith(pinAutoSubmits)
  }

  override fun showIncompletePinError() {
    pinHintTextView.visibility = View.GONE
    errorTextView.visibility = View.VISIBLE
    errorTextView.text = resources.getString(R.string.registrationpin_error_incomplete_pin)
  }

  override fun hideIncompletePinError() {
    pinHintTextView.visibility = View.VISIBLE
    errorTextView.visibility = View.GONE
  }

  override fun openRegistrationConfirmPinScreen(registrationEntry: OngoingRegistrationEntry) {
    router.push(RegistrationConfirmPinScreenKey(registrationEntry))
  }

  interface Injector {
    fun inject(target: RegistrationPinScreen)
  }
}
