package org.simple.clinic.enterotp

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.StaggeredEditText
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class EnterOtpScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

  @Inject
  lateinit var controller: EnterOtpScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val userPhoneNumberTextView by bindView<TextView>(R.id.enterotp_phonenumber)
  private val otpEntryEditText by bindView<StaggeredEditText>(R.id.enterotp_otp)
  private val backButton by bindView<ImageButton>(R.id.enterotp_back)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.merge(screenCreates(), backClicks())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    otpEntryEditText.showKeyboard()
  }

  private fun screenCreates() = Observable.just(EnterOtpScreenCreated())

  private fun backClicks() = RxView.clicks(backButton).map { EnterOtpBackClicked() }

  fun showUserPhoneNumber(phoneNumber: String) {
    val phoneNumberWithCountryCode = resources.getString(
        R.string.enterotp_phonenumber,
        resources.getString(R.string.country_dialing_code),
        phoneNumber
    )

    userPhoneNumberTextView.text = phoneNumberWithCountryCode
  }

  fun goBack() {
    screenRouter.pop()
  }
}
