package org.resolvetosavelives.red.newentry

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.patient.Gender
import org.resolvetosavelives.red.patient.OngoingPatientEntry
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.util.Just
import org.resolvetosavelives.red.util.None
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent
import org.resolvetosavelives.red.widgets.setTextAndCursor
import org.resolvetosavelives.red.widgets.showKeyboard
import javax.inject.Inject

class PatientEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientEntryScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientEntryScreenController

  private val upButton by bindView<View>(R.id.patiententry_up)
  private val formScrollView by bindView<ScrollView>(R.id.patiententry_form_scrollable_container)
  private val fullNameEditText by bindView<EditText>(R.id.patiententry_full_name)
  private val phoneNumberEditText by bindView<EditText>(R.id.patiententry_phone_number)
  private val noPhoneNumberCheckBox by bindView<CheckBox>(R.id.patiententry_phone_number_none)
  private val dateOfBirthEditText by bindView<EditText>(R.id.patiententry_date_of_birth)
  private val ageEditText by bindView<EditText>(R.id.patiententry_age)
  private val genderRadioGroup by bindView<RadioGroup>(R.id.patiententry_gender_radiogroup)
  private val colonyOrVillageEditText by bindView<EditText>(R.id.patiententry_colony_or_village)
  private val noColonyOrVillageCheckBox by bindView<CheckBox>(R.id.patiententry_colony_or_village_none)
  private val districtEditText by bindView<EditText>(R.id.patiententry_district)
  private val stateEditText by bindView<EditText>(R.id.patiententry_state)
  private val saveButton by bindView<View>(R.id.patiententry_save)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    // Plan:
    // - Change date format to DD/MM/YYYY from DD-MM-YYYY.
    // - Animate date-of-birth and age.
    // - Show 'X' icon when a field is focused.

    fullNameEditText.showKeyboard()
    upButton.setOnClickListener { screenRouter.pop() }

    // Save button is also disabled by the controller as soon as it starts emitting
    // UiChanges, but by the time that happens, the save button is visible on the
    // screen for a moment. Disabling it here solves the problem.
    setSaveButtonEnabled(false)

    Observable
        .mergeArray(
            screenCreates(),
            formChanges(),
            saveClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun formChanges(): Observable<UiEvent> {
    return Observable.mergeArray(
        fullNameEditText.textChanges(::PatientFullNameTextChanged),
        phoneNumberEditText.textChanges(::PatientPhoneNumberTextChanged),
        RxCompoundButton.checkedChanges(noPhoneNumberCheckBox).map(::PatientNoPhoneNumberToggled),
        dateOfBirthEditText.textChanges(::PatientDateOfBirthTextChanged),
        ageEditText.textChanges(::PatientAgeTextChanged),
        colonyOrVillageEditText.textChanges(::PatientColonyOrVillageTextChanged),
        RxCompoundButton.checkedChanges(noColonyOrVillageCheckBox).map(::PatientNoColonyOrVillageToggled),
        districtEditText.textChanges(::PatientDistrictTextChanged),
        stateEditText.textChanges(::PatientStateTextChanged),
        genderChanges())
  }

  private fun genderChanges(): Observable<PatientGenderChanged> {
    val radioIdToGenders = mapOf(
        R.id.patiententry_gender_female to Gender.FEMALE,
        R.id.patiententry_gender_male to Gender.MALE,
        R.id.patiententry_gender_transgender to Gender.TRANSGENDER)

    return RxRadioGroup.checkedChanges(genderRadioGroup)
        .map { checkedId ->
          val gender = radioIdToGenders[checkedId]
          when (gender) {
            null -> PatientGenderChanged(None)
            else -> PatientGenderChanged(Just(gender))
          }
        }
  }

  private fun saveClicks() = RxView.clicks(saveButton).map { PatientEntrySaveClicked() }

  fun preFillFields(details: OngoingPatientEntry) {
    fullNameEditText.setTextAndCursor(details.personalDetails?.fullName)
    phoneNumberEditText.setTextAndCursor(details.phoneNumber?.number)
    dateOfBirthEditText.setTextAndCursor(details.personalDetails?.dateOfBirth)
    ageEditText.setTextAndCursor(details.personalDetails?.age)
    colonyOrVillageEditText.setTextAndCursor(details.address?.colonyOrVillage)
    districtEditText.setTextAndCursor(details.address?.district)
    stateEditText.setTextAndCursor(details.address?.state)
  }

  fun setSaveButtonEnabled(enabled: Boolean) {
    if (!ViewCompat.isLaidOut(saveButton)) {
      saveButton.visibility = when (enabled) {
        true -> View.VISIBLE
        false -> View.INVISIBLE
      }

    } else {
      if (enabled) {
        saveButton.translationY = saveButton.height.toFloat()
        saveButton.animate()
            .translationY(0f)
            .setInterpolator(FastOutSlowInInterpolator())
            .withStartAction { saveButton.visibility = View.VISIBLE }
            .start()

      } else {
        saveButton.animate()
            .translationY(saveButton.height.toFloat())
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
      }
    }

    if (enabled) {
      // The save button covers the district and state fields when it shows up.
      // Force-scrolling to the bottom solves this problem. Not the best
      // solution, but works for now.
      if (colonyOrVillageEditText.isFocused || districtEditText.isFocused || stateEditText.isFocused) {
        formScrollView.smoothScrollTo(0, formScrollView.height)
      }
    }
  }

  fun openSummaryScreenForBpEntry() {
    // TODO.
  }

  fun resetPhoneNumberField() {
    phoneNumberEditText.text = null
  }

  fun resetColonyOrVillageField() {
    colonyOrVillageEditText.text = null
  }

  fun uncheckNoPhoneNumberCheckbox() {
    noPhoneNumberCheckBox.isChecked = false
  }

  fun uncheckNoVillageOrColonyCheckbox() {
    noColonyOrVillageCheckBox.isChecked = false
  }
}

private fun <T> EditText.textChanges(mapper: (String) -> T): Observable<T> {
  return RxTextView.textChanges(this)
      .map { it.toString() }
      .map(mapper)
}
