package org.simple.clinic.registration.name

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.registration.pin.RegistrationPinScreen
import org.simple.clinic.router.screen.ScreenRouter
import javax.inject.Inject

class RegistrationFullNameScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RegistrationFullNameScreenController

  private val backButton by bindView<ImageButton>(R.id.registrationname_back)
  private val nameEditText by bindView<EditText>(R.id.registrationname_name)
  private val nextButton by bindView<Button>(R.id.registrationname_next)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    backButton.setOnClickListener {
      screenRouter.pop()
    }

    Observable.merge(nameTextChanges(), nextClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun nameTextChanges() =
      RxTextView.textChanges(nameEditText)
          .map(CharSequence::toString)
          .map(::RegistrationFullNameTextChanged)

  private fun nextClicks() =
      RxView.clicks(nextButton)
          .map { RegistrationFullNameNextClicked() }

  fun openRegistrationNameEntryScreen() {
    screenRouter.push(RegistrationPinScreen.KEY)
  }

  fun setNextButtonEnabled(enabled: Boolean) {
    nextButton.isEnabled = enabled
  }

  companion object {
    val KEY = RegistrationNameScreenKey()
  }
}
