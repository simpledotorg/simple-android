package org.simple.clinic.registration.pin

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import com.jakewharton.rxbinding3.widget.editorActions
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.SECURITY_PIN_LENGTH
import org.simple.clinic.databinding.ScreenRegistrationPinBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class RegistrationPinScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), RegistrationPinUi, RegistrationPinUiActions {

  var binding: ScreenRegistrationPinBinding? = null

  private val pinEditText
    get() = binding!!.pinEditText

  private val backButton
    get() = binding!!.backButton

  private val pinHintTextView
    get() = binding!!.pinHintTextView

  private val errorTextView
    get() = binding!!.errorTextView

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandlerFactory: RegistrationPinEffectHandler.Factory

  private val events by unsafeLazy {
    Observable
        .merge(
            pinTextChanges(),
            doneClicks()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate: MobiusDelegate<RegistrationPinModel, RegistrationPinEvent, RegistrationPinEffect> by unsafeLazy {
    val uiRenderer = RegistrationPinUiRenderer(this)
    val screenKey = screenRouter.key<RegistrationPinScreenKey>(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = RegistrationPinModel.create(screenKey.registrationEntry),
        update = RegistrationPinUpdate(requiredPinLength = SECURITY_PIN_LENGTH),
        init = RegistrationPinInit(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    binding = ScreenRegistrationPinBinding.bind(this)
    if (isInEditMode) {
      return
    }
    context.injector<Injector>().inject(this)

    pinEditText.isSaveEnabled = false

    backButton.setOnClickListener {
      screenRouter.pop()
    }

    post { pinEditText.requestFocus() }
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
    screenRouter.push(RegistrationConfirmPinScreenKey(registrationEntry))
  }

  interface Injector {
    fun inject(target: RegistrationPinScreen)
  }
}
