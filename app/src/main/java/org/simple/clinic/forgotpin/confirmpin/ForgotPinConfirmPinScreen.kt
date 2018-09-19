package org.simple.clinic.forgotpin.confirmpin

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.facility.change.FacilityChangeScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class ForgotPinConfirmPinScreen(context: Context, attributeSet: AttributeSet?) : RelativeLayout(context, attributeSet) {

  @Inject
  lateinit var controller: ForgotPinConfirmPinScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val backButton by bindView<ImageButton>(R.id.forgotpin_back)
  private val facilityNameTextView by bindView<TextView>(R.id.forgotpin_facility_name)
  private val userNameTextView by bindView<TextView>(R.id.forgotpin_user_fullname)

  override fun onFinishInflate() {
    super.onFinishInflate()

    TheActivity.component.inject(this)

    Observable.merge(screenCreates(), facilityClicks(), backClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { it.invoke(this) }
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated())

  private fun facilityClicks(): Observable<UiEvent> =
      RxView.clicks(facilityNameTextView)
          .map { ForgotPinConfirmPinScreenFacilityClicked }

  private fun backClicks(): Observable<UiEvent> =
      RxView.clicks(backButton)
          .map { ForgotPinConfirmPinScreenBackClicked }

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
}
