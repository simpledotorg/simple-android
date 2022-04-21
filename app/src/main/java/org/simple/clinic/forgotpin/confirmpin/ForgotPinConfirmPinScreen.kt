package org.simple.clinic.forgotpin.confirmpin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import com.jakewharton.rxbinding3.widget.editorActions
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenForgotpinConfirmpinBinding
import org.simple.clinic.di.injector
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import org.simple.clinic.widgets.textChanges
import javax.inject.Inject

class ForgotPinConfirmPinScreen : BaseScreen<
    ForgotPinConfirmPinScreen.Key,
    ScreenForgotpinConfirmpinBinding,
    ForgotPinConfirmPinModel,
    ForgotPinConfirmPinEvent,
    ForgotPinConfirmPinEffect,
    ForgotPinConfirmPinViewEffect>(), ForgotPinConfirmPinUi, ForgotPinConfirmPinUiActions {

  @Inject
  lateinit var effectHandlerFactory: ForgotPinConfirmPinEffectHandler.Factory

  @Inject
  lateinit var router: Router

  private val pinEntryEditText
    get() = binding.pinEntryEditText

  private val backButton
    get() = binding.backButton

  private val userNameTextView
    get() = binding.userNameTextView

  private val facilityNameTextView
    get() = binding.facilityNameTextView

  private val pinErrorTextView
    get() = binding.pinErrorTextView

  private val pinEntryHintTextView
    get() = binding.pinEntryHintTextView

  private val progressBar
    get() = binding.progressBar

  private val pinEntryContainer
    get() = binding.pinEntryContainer

  override fun defaultModel() = ForgotPinConfirmPinModel.create(previousPin = screenKey.enteredPin)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenForgotpinConfirmpinBinding.inflate(layoutInflater, container, false)

  override fun events(): Observable<ForgotPinConfirmPinEvent> {
    return Observable
        .merge(
            pinSubmits(),
            pinTextChanges()
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun createUpdate() = ForgotPinConfirmPinUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<ForgotPinConfirmPinViewEffect>) = effectHandlerFactory
      .create(this)
      .build()

  override fun createInit() = ForgotPinConfirmPinInit()

  override fun uiRenderer() = ForgotPinConfirmPinUiRenderer(this)

  override fun viewEffectHandler() = ForgotPinConfirmPinViewEffectHandler(uiActions = this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    pinEntryEditText.showKeyboard()
    backButton.setOnClickListener { goBack() }
  }

  private fun pinSubmits() =
      pinEntryEditText
          .editorActions { it == EditorInfo.IME_ACTION_DONE }
          .map { ForgotPinConfirmPinSubmitClicked(pinEntryEditText.text.toString()) }

  private fun pinTextChanges() =
      pinEntryEditText
          .textChanges { ForgotPinConfirmPinTextChanged(it) }

  override fun showUserName(name: String) {
    userNameTextView.text = name
  }

  override fun showFacility(name: String) {
    facilityNameTextView.text = name
  }

  private fun goBack() {
    router.pop()
  }

  override fun showPinMismatchedError() {
    showError(R.string.forgotpin_error_pin_mismatch)
  }

  override fun showUnexpectedError() {
    showError(R.string.api_unexpected_error)
  }

  override fun showNetworkError() {
    showError(R.string.api_network_error)
  }

  override fun hideError() {
    pinErrorTextView.visibility = GONE
    pinEntryHintTextView.visibility = VISIBLE
  }

  override fun showProgress() {
    progressBar.visibility = VISIBLE
    pinEntryContainer.visibility = INVISIBLE
    pinEntryContainer.hideKeyboard()
  }

  private fun hideProgress() {
    progressBar.visibility = INVISIBLE
    pinEntryContainer.visibility = VISIBLE
  }

  override fun goToHomeScreen() {
    router.clearHistoryAndPush(HomeScreenKey)
  }

  private fun showError(@StringRes errorMessageResId: Int) {
    hideProgress()
    pinEntryHintTextView.visibility = GONE
    pinErrorTextView.setText(errorMessageResId)
    pinErrorTextView.visibility = VISIBLE
    pinEntryEditText.showKeyboard()
  }

  interface Injector {
    fun inject(target: ForgotPinConfirmPinScreen)
  }

  @Parcelize
  data class Key(
      val enteredPin: String,
      override val analyticsName: String = "Forgot PIN Confirm PIN"
  ) : ScreenKey() {
    override fun instantiateFragment() = ForgotPinConfirmPinScreen()
  }
}
