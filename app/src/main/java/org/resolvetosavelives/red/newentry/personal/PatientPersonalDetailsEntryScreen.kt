package org.resolvetosavelives.red.newentry.personal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.address.PatientAddressEntryScreen
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.widgets.showKeyboard
import javax.inject.Inject

class PatientPersonalDetailsEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientPersonalDetailsScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientPersonalDetailsEntryScreenController

  private val fullNameEditText by bindView<EditText>(R.id.patientpersonaldetails_full_name)
  private val proceedButton by bindView<View>(R.id.patientpersonaldetails_next_button)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.merge(fullNameTextChanges(), proceedClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun fullNameTextChanges() = RxTextView.textChanges(fullNameEditText)
      .map(CharSequence::toString)
      .map(::PatientFullNameTextChanged)

  private fun proceedClicks() = RxView.clicks(proceedButton)
      .map { PatientPersonalDetailsProceedClicked() }

  fun showKeyboardOnFullnameField() {
    fullNameEditText.showKeyboard()
  }

  fun openAddressEntryScreen() {
    screenRouter.push(PatientAddressEntryScreen.KEY)
  }
}
