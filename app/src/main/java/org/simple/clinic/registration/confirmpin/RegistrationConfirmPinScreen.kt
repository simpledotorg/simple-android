package org.simple.clinic.registration.confirmpin

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.SECURITY_PIN_LENGTH
import org.simple.clinic.databinding.ScreenRegistrationConfirmPinBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.registration.location.RegistrationLocationPermissionScreenKey
import org.simple.clinic.registration.pin.RegistrationPinScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class RegistrationConfirmPinScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), RegistrationConfirmPinUi, RegistrationConfirmPinUiActions {

  var binding: ScreenRegistrationConfirmPinBinding? = null

  private val confirmPinEditText
    get() = binding!!.confirmPinEditText

  private val backButton
    get() = binding!!.backButton

  private val resetPinButton
    get() = binding!!.resetPinButton

  private val errorStateViewGroup
    get() = binding!!.errorStateViewGroup

  private val pinHintTextView
    get() = binding!!.pinHintTextView

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  @Inject
  lateinit var effectHandlerFactory: RegistrationConfirmPinEffectHandler.Factory

  private val events by unsafeLazy {
    Observable
        .merge(
            confirmPinTextChanges(),
            resetPinClicks(),
            doneClicks()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = RegistrationConfirmPinUiRenderer(this)
    val screenKey = screenKeyProvider.keyFor<RegistrationConfirmPinScreenKey>(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = RegistrationConfirmPinModel.create(screenKey.registrationEntry),
        update = RegistrationConfirmPinUpdate(),
        init = RegistrationConfirmPinInit(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    binding = ScreenRegistrationConfirmPinBinding.bind(this)
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    backButton.setOnClickListener {
      screenRouter.pop()
    }

    // Showing the keyboard again in case the user returns from location permission screen.
    confirmPinEditText.showKeyboard()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    binding = null
    delegate.stop()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
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
    hideKeyboard()
    screenRouter.push(RegistrationLocationPermissionScreenKey(entry))
  }

  override fun goBackToPinScreen(entry: OngoingRegistrationEntry) {
    screenRouter.replaceKeyOfSameType(RegistrationPinScreenKey(entry))
  }

  interface Injector {
    fun inject(target: RegistrationConfirmPinScreen)
  }
}
