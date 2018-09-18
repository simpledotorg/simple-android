package org.simple.clinic.forgotpin.createnewpin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
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
import org.simple.clinic.forgotpin.confirmpin.ForgotPinConfirmPinScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.StaggeredEditText
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class ForgotPinCreateNewPinScreen(context: Context, attributeSet: AttributeSet?) : RelativeLayout(context, attributeSet) {

  @Inject
  lateinit var controller: ForgotPinCreateNewPinScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val userNameTextView by bindView<TextView>(R.id.forgotpin_user_fullname)
  private val facilityNameTextView by bindView<TextView>(R.id.forgotpin_facility_name)
  private val pinEntryEditText by bindView<StaggeredEditText>(R.id.forgotpin_pin)
  private val pinErrorTextView by bindView<TextView>(R.id.forgotpin_error)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    Observable.merge(screenCreates(), pinTextChanges(), pinSubmitClicked(), facilityClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { it.invoke(this) }

    pinEntryEditText.showKeyboard()
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated())

  private fun pinTextChanges(): Observable<UiEvent> {
    return RxTextView.textChanges(pinEntryEditText)
        .map { ForgotPinCreateNewPinTextChanged(it.toString()) }
  }

  private fun pinSubmitClicked(): Observable<UiEvent> {
    return RxTextView.editorActions(pinEntryEditText)
        .filter { it == EditorInfo.IME_ACTION_DONE }
        .map { ForgotPinCreateNewPinSubmitClicked }
  }

  private fun facilityClicks(): Observable<UiEvent> {
    return RxView.clicks(facilityNameTextView).map { ForgotPinCreateNewPinFacilityClicked }
  }

  fun showUserName(name: String) {
    userNameTextView.text = name
  }

  fun showFacility(name: String) {
    facilityNameTextView.text = name
  }

  fun showInvalidPinError() {
    pinErrorTextView.visibility = View.VISIBLE
  }

  fun showConfirmPinScreen(pin: String) {
    hideKeyboard()
    screenRouter.push(ForgotPinConfirmPinScreenKey(pin))
  }

  fun hideInvalidPinError() {
    pinErrorTextView.visibility = View.GONE
  }

  fun openFacilityChangeScreen() {
    screenRouter.push(FacilityChangeScreenKey())
  }
}
