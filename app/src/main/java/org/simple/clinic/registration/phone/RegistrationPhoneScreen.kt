package org.simple.clinic.registration.phone

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.registration.name.RegistrationFullNameScreen
import org.simple.clinic.router.screen.ScreenRouter
import javax.inject.Inject

class RegistrationPhoneScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationPhoneScreenController

  private val phoneNumberEditText by bindView<EditText>(R.id.registrationphone_phone)
  private val nextButton by bindView<Button>(R.id.registrationphone_next)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.merge(screenCreates(), phoneNumberTextChanges(), nextClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(RegistrationPhoneScreenCreated())

  private fun phoneNumberTextChanges() =
      RxTextView.textChanges(phoneNumberEditText)
          .map(CharSequence::toString)
          .map(::RegistrationPhoneNumberTextChanged)

  private fun nextClicks() =
      RxView.clicks(nextButton)
          .map { RegistrationPhoneNextClicked() }

  fun openRegistrationNameEntryScreen() {
    screenRouter.push(RegistrationFullNameScreen.KEY)
  }

  fun setNextButtonEnabled(enabled: Boolean) {
    nextButton.isEnabled = enabled
  }

  companion object {
    val KEY = RegistrationPhoneScreenKey()
  }
}
