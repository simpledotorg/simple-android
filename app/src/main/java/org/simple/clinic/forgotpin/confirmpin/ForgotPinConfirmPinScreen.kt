package org.simple.clinic.forgotpin.confirmpin

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.main.TheActivity
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class ForgotPinConfirmPinScreen(context: Context, attributeSet: AttributeSet?) : RelativeLayout(context, attributeSet), ForgotPinConfirmPinUi {

  @Inject
  lateinit var controller: ForgotPinConfirmPinScreenController.Factory

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val backButton by bindView<ImageButton>(R.id.forgotpin_confirmpin_back)
  private val progressBar by bindView<ProgressBar>(R.id.forgotpin_confirmpin_progress)
  private val facilityNameTextView by bindView<TextView>(R.id.forgotpin_confirmpin_facility_name)
  private val userNameTextView by bindView<TextView>(R.id.forgotpin_confirmpin_user_fullname)
  private val pinEntryEditText by bindView<EditText>(R.id.forgotpin_confirmpin_pin)
  private val pinErrorTextView by bindView<TextView>(R.id.forgotpin_confirmpin_error)
  private val pinEntryContainer by bindView<ViewGroup>(R.id.forgotpin_confirmpin_pin_container)
  private val pinEntryHintTextView by bindView<TextView>(R.id.forgotpin_confirmpin_confirm_message)

  override fun onFinishInflate() {
    super.onFinishInflate()

    TheActivity.component.inject(this)

    val screenKey = screenRouter.key<ForgotPinConfirmPinScreenKey>(this)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            pinSubmits(),
            pinTextChanges()
        ),
        controller = controller.create(screenKey.enteredPin),
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    pinEntryEditText.showKeyboard()

    backButton.setOnClickListener { goBack() }
  }

  private fun screenCreates(): Observable<UiEvent> {
    return Observable.just(ScreenCreated())
  }

  private fun pinSubmits() =
      RxTextView.editorActions(pinEntryEditText)
          .filter { it == EditorInfo.IME_ACTION_DONE }
          .map { ForgotPinConfirmPinSubmitClicked(pinEntryEditText.text.toString()) }

  private fun pinTextChanges() =
      RxTextView.textChanges(pinEntryEditText)
          .map { ForgotPinConfirmPinTextChanged(it.toString()) }

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
    screenRouter.clearHistoryAndPush(HomeScreenKey(), RouterDirection.FORWARD)
  }

  private fun showError(@StringRes errorMessageResId: Int) {
    hideProgress()
    pinEntryHintTextView.visibility = GONE
    pinErrorTextView.setText(errorMessageResId)
    pinErrorTextView.visibility = VISIBLE
    pinEntryEditText.showKeyboard()
  }
}
