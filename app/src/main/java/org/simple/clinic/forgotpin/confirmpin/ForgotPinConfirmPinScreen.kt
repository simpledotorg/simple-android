package org.simple.clinic.forgotpin.confirmpin

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.facility.change.FacilityChangeScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class ForgotPinConfirmPinScreen(context: Context, attributeSet: AttributeSet?) : RelativeLayout(context, attributeSet) {

  @Inject
  lateinit var controller: ForgotPinConfirmPinScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val backButton by bindView<ImageButton>(R.id.forgotpin_back)
  private val facilityNameTextView by bindView<TextView>(R.id.forgotpin_facility_name)
  private val userNameTextView by bindView<TextView>(R.id.forgotpin_user_fullname)
  private val pinEntryEditText by bindView<EditText>(R.id.forgotpin_pin)
  private val pinErrorTextView by bindView<TextView>(R.id.forgotpin_error)

  override fun onFinishInflate() {
    super.onFinishInflate()

    TheActivity.component.inject(this)

    Observable.mergeArray(screenCreates(), facilityClicks(), backClicks(), pinSubmits(), pinTextChanges())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { it.invoke(this) }

    pinEntryEditText.showKeyboard()
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<ForgotPinConfirmPinScreenKey>(this)
    return Observable.just(ForgotPinConfirmPinScreenCreated(screenKey.enteredPin))
  }

  private fun facilityClicks(): Observable<UiEvent> =
      RxView.clicks(facilityNameTextView)
          .map { ForgotPinConfirmPinScreenFacilityClicked }

  private fun backClicks(): Observable<UiEvent> =
      RxView.clicks(backButton)
          .map { ForgotPinConfirmPinScreenBackClicked }

  private fun pinSubmits(): Observable<UiEvent> {
    return RxTextView.editorActions(pinEntryEditText)
        .filter { it == EditorInfo.IME_ACTION_DONE }
        .map { ForgotPinConfirmPinSubmitClicked(pinEntryEditText.text.toString()) }
  }

  private fun pinTextChanges(): Observable<UiEvent> {
    return RxTextView.textChanges(pinEntryEditText)
        .map { ForgotPinConfirmPinTextChanged(it.toString()) }
  }

  fun showUserName(name: String) {
    userNameTextView.text = name
  }

  fun showFacility(name: String) {
    facilityNameTextView.text = name
  }

  fun openFacilityChangeScreen() {
    screenRouter.push(FacilityChangeScreenKey())
  }

  fun goBack() {
    screenRouter.pop()
  }

  fun showPinMismatchedError() {
    pinErrorTextView.visibility = VISIBLE
  }

  fun hideError() {
    pinErrorTextView.visibility = GONE
  }
}
