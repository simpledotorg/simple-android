package org.resolvetosavelives.red.newentry.personal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.address.PatientAddressEntryScreen
import org.resolvetosavelives.red.newentry.search.Gender
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

  private val fullNameEditText by bindView<EditText>(R.id.patiententry_personal_full_name)
  private val dateOfBirthEditText by bindView<EditText>(R.id.patiententry_personal_dateofbirth)
  private val ageEditText by bindView<EditText>(R.id.patiententry_personal_age)
  private val genderRadioGroup by bindView<RadioGroup>(R.id.patiententry_personal_gender_radiogroup)
  private val proceedButton by bindView<View>(R.id.patiententry_personal_proceed)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable
        .mergeArray(fullNameTextChanges(), dateOfBirthTextChanges(), ageTextChanges(), genderChanges(), proceedClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun fullNameTextChanges() = RxTextView.textChanges(fullNameEditText)
      .map(CharSequence::toString)
      .map(::PatientFullNameTextChanged)

  private fun dateOfBirthTextChanges() = RxTextView.textChanges(dateOfBirthEditText)
      .map(CharSequence::toString)
      .map(::PatientDateOfBirthTextChanged)

  private fun ageTextChanges() = RxTextView.textChanges(ageEditText)
      .map(CharSequence::toString)
      .map(Integer::parseInt)
      .map(::PatientAgeTextChanged)

  private fun genderChanges() = RxRadioGroup.checkedChanges(genderRadioGroup)
      .map {
        when (it) {
          R.id.patiententry_personal_gender_female -> Gender.FEMALE
          R.id.patiententry_personal_gender_male -> Gender.MALE
          R.id.patiententry_personal_gender_trans -> Gender.TRANS
          else -> throw AssertionError("Unknown gender radio: " + resources.getResourceEntryName(it))
        }
      }
      .map { gender -> PatientGenderChanged(gender) }

  private fun proceedClicks() = RxView.clicks(proceedButton)
      .map { PatientPersonalDetailsProceedClicked() }

  fun showKeyboardOnFullnameField() {
    fullNameEditText.showKeyboard()
  }

  fun openAddressEntryScreen() {
    screenRouter.push(PatientAddressEntryScreen.KEY)
  }
}
