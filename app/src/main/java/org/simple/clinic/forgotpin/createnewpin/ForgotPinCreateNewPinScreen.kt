package org.simple.clinic.forgotpin.createnewpin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding3.widget.editorActions
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenForgotpinCreatepinBinding
import org.simple.clinic.di.injector
import org.simple.clinic.forgotpin.confirmpin.ForgotPinConfirmPinScreen
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class ForgotPinCreateNewPinScreen : BaseScreen<
    ForgotPinCreateNewPinScreen.Key,
    ScreenForgotpinCreatepinBinding,
    ForgotPinCreateNewModel,
    ForgotPinCreateNewEvent,
    ForgotPinCreateNewEffect,
    ForgotPinCreateNewViewEffect>(), ForgotPinCreateNewPinUi, UiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: ForgotPinCreateNewEffectHandler.Factory

  private val createPinEditText
    get() = binding.createPinEditText

  private val userFullNameTextView
    get() = binding.userFullNameTextView

  private val facilityNameTextView
    get() = binding.facilityNameTextView

  private val createPinErrorTextView
    get() = binding.createPinErrorTextView

  private val appLockRoot
    get() = binding.applockRoot

  override fun defaultModel() = ForgotPinCreateNewModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenForgotpinCreatepinBinding.inflate(layoutInflater, container, false)

  override fun createUpdate() = ForgotPinCreateNewUpdate()

  override fun createInit() = ForgotPinCreateNewInit()

  override fun events(): Observable<ForgotPinCreateNewEvent> {
    return Observable
        .merge(
            pinTextChanges(),
            pinSubmitClicked()
        )
        .compose(ReportAnalyticsEvents())
        .cast<ForgotPinCreateNewEvent>()
  }

  override fun createEffectHandler(viewEffectsConsumer: Consumer<ForgotPinCreateNewViewEffect>) = effectHandlerFactory
      .create(viewEffectsConsumer = viewEffectsConsumer)
      .build()

  override fun uiRenderer() = ForgotPinCreateNewUiRenderer(this)

  override fun viewEffectHandler() = ForgotPinCreateNewViewEffectHandler(uiActions = this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    createPinEditText.showKeyboard()
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
    appLockRoot.hideKeyboard()
    router.push(ForgotPinConfirmPinScreen.Key(pin))
  }

  override fun hideInvalidPinError() {
    createPinErrorTextView.visibility = View.GONE
  }

  interface Injector {
    fun inject(target: ForgotPinCreateNewPinScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Forgot PIN Create New PIN"
  ) : ScreenKey() {
    override fun instantiateFragment(): Fragment = ForgotPinCreateNewPinScreen()
  }
}
