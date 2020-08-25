package org.simple.clinic.forgotpin.confirmpin

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import com.jakewharton.rxbinding3.widget.editorActions
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_forgotpin_confirmpin.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import org.simple.clinic.widgets.textChanges
import javax.inject.Inject

class ForgotPinConfirmPinScreen(
    context: Context,
    attributeSet: AttributeSet?
) : RelativeLayout(context, attributeSet), ForgotPinConfirmPinUi, ForgotPinConfirmPinUiActions {

  @Inject
  lateinit var effectHandlerFactory: ForgotPinConfirmPinEffectHandler.Factory

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val events by unsafeLazy {
    Observable
        .merge(
            pinSubmits(),
            pinTextChanges()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val screenKey = screenRouter.key<ForgotPinConfirmPinScreenKey>(this)
    val uiRenderer = ForgotPinConfirmPinUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = ForgotPinConfirmPinModel.create(previousPin = screenKey.enteredPin),
        init = ForgotPinConfirmPinInit(),
        update = ForgotPinConfirmPinUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    context.injector<Injector>().inject(this)

    pinEntryEditText.showKeyboard()

    backButton.setOnClickListener { goBack() }
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
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
    screenRouter.pop()
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
    hideKeyboard()
  }

  private fun hideProgress() {
    progressBar.visibility = INVISIBLE
    pinEntryContainer.visibility = VISIBLE
  }

  override fun goToHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey, RouterDirection.FORWARD)
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
}
