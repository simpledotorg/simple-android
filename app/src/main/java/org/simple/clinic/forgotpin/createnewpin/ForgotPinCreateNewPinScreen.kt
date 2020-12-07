package org.simple.clinic.forgotpin.createnewpin

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import com.jakewharton.rxbinding3.widget.editorActions
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenForgotpinCreatepinBinding
import org.simple.clinic.di.injector
import org.simple.clinic.forgotpin.confirmpin.ForgotPinConfirmPinScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class ForgotPinCreateNewPinScreen(
    context: Context,
    attributeSet: AttributeSet?
) : RelativeLayout(context, attributeSet), ForgotPinCreateNewPinUi, UiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandlerFactory: ForgotPinCreateNewEffectHandler.Factory

  private var binding: ScreenForgotpinCreatepinBinding? = null

  private val createPinEditText
    get() = binding!!.createPinEditText

  private val userFullNameTextView
    get() = binding!!.userFullNameTextView

  private val facilityNameTextView
    get() = binding!!.facilityNameTextView

  private val createPinErrorTextView
    get() = binding!!.createPinErrorTextView

  private val events by unsafeLazy {
    Observable
        .merge(
            pinTextChanges(),
            pinSubmitClicked()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = ForgotPinCreateNewUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = ForgotPinCreateNewModel.create(),
        update = ForgotPinCreateNewUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = ForgotPinCreateNewInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenForgotpinCreatepinBinding.bind(this)

    context.injector<Injector>().inject(this)

    createPinEditText.showKeyboard()
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

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun pinTextChanges() =
      createPinEditText
          .textChanges()
          .map { ForgotPinCreateNewPinTextChanged(it.toString()) }

  private fun pinSubmitClicked() =
      createPinEditText
          .editorActions { it == EditorInfo.IME_ACTION_DONE }
          .map { ForgotPinCreateNewPinSubmitClicked }

  override fun showUserName(name: String) {
    userFullNameTextView.text = name
  }

  override fun showFacility(name: String) {
    facilityNameTextView.text = name
  }

  override fun showInvalidPinError() {
    createPinErrorTextView.visibility = View.VISIBLE
  }

  override fun showConfirmPinScreen(pin: String) {
    hideKeyboard()
    screenRouter.push(ForgotPinConfirmPinScreenKey(pin))
  }

  override fun hideInvalidPinError() {
    createPinErrorTextView.visibility = View.GONE
  }

  interface Injector {
    fun inject(target: ForgotPinCreateNewPinScreen)
  }
}
