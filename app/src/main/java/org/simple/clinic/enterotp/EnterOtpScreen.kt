package org.simple.clinic.enterotp

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.PinEditText
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class EnterOtpScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

  @Inject
  lateinit var controller: EnterOtpScreenController

  private val userPhoneNumberTextView by bindView<TextView>(R.id.enterotp_phonenumber)
  private val otpEntryEditText by bindView<PinEditText>(R.id.enterotp_otp)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    screenCreates()
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    otpEntryEditText.showKeyboard()
  }

  private fun screenCreates() = Observable.just(EnterOtpScreenCreated())

  fun showUserPhoneNumber(phoneNumber: String) {
    val phoneNumberWithCountryCode = resources.getString(
        R.string.enterotp_phonenumber,
        resources.getString(R.string.country_dialing_code),
        phoneNumber
    )

    userPhoneNumberTextView.text = phoneNumberWithCountryCode
  }
}
