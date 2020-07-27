package org.simple.clinic.forgotpin.createnewpin

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.forgotpin.confirmpin.ForgotPinConfirmPinScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.StaggeredEditText
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class ForgotPinCreateNewPinScreen(context: Context, attributeSet: AttributeSet?) : RelativeLayout(context, attributeSet), ForgotPinCreateNewPinUi {

  @Inject
  lateinit var controller: ForgotPinCreateNewPinScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandlerFactory: ForgotPinCreateNewEffectHandler.Factory

  private val userNameTextView by bindView<TextView>(R.id.forgotpin_createpin_user_fullname)
  private val facilityNameTextView by bindView<TextView>(R.id.forgotpin_createpin_facility_name)
  private val pinEntryEditText by bindView<StaggeredEditText>(R.id.forgotpin_createpin_pin)
  private val pinErrorTextView by bindView<TextView>(R.id.forgotpin_createpin_error)

  private val events by unsafeLazy {
    Observable
        .merge(
            screenCreates(),
            pinTextChanges(),
            pinSubmitClicked()
        )
        .compose(ReportAnalyticsEvents())
        .share()
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

    context.injector<Injector>().inject(this)

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    pinEntryEditText.showKeyboard()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated())

  private fun pinTextChanges() =
      RxTextView.textChanges(pinEntryEditText)
          .map { ForgotPinCreateNewPinTextChanged(it.toString()) }

  private fun pinSubmitClicked() =
      RxTextView.editorActions(pinEntryEditText)
          .filter { it == EditorInfo.IME_ACTION_DONE }
          .map { ForgotPinCreateNewPinSubmitClicked }

  override fun showUserName(name: String) {
    userNameTextView.text = name
  }

  override fun showFacility(name: String) {
    facilityNameTextView.text = name
  }

  override fun showInvalidPinError() {
    pinErrorTextView.visibility = View.VISIBLE
  }

  override fun showConfirmPinScreen(pin: String) {
    hideKeyboard()
    screenRouter.push(ForgotPinConfirmPinScreenKey(pin))
  }

  override fun hideInvalidPinError() {
    pinErrorTextView.visibility = View.GONE
  }

  interface Injector {
    fun inject(target: ForgotPinCreateNewPinScreen)
  }
}
