package org.resolvetosavelives.red.newentry

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
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
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent
import org.resolvetosavelives.red.widgets.setTextAndCursor
import org.resolvetosavelives.red.widgets.showKeyboard
import javax.inject.Inject

class PatientEntryScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  companion object {
    val KEY = PatientEntryScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientEntryScreenController

  private val upButton by bindView<View>(R.id.patiententry_up)
  private val fullNameEditText by bindView<EditText>(R.id.patiententry_full_name)
  private val phoneNumberEditText by bindView<EditText>(R.id.patiententry_phone_number)
  private val dateOfBirthEditText by bindView<EditText>(R.id.patiententry_date_of_birth)
  private val ageEditText by bindView<EditText>(R.id.patiententry_age)
  private val genderRadioGroup by bindView<RadioGroup>(R.id.patiententry_gender_radiogroup)
  private val colonyOrVillageEditText by bindView<EditText>(R.id.patiententry_colony_or_village)
  private val districtEditText by bindView<EditText>(R.id.patiententry_district)
  private val stateEditText by bindView<EditText>(R.id.patiententry_state)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    // Plan:
    // 2. When do we enable save button?
    // 3. Save patient
    // 4. Handle 'none'.
    // 5. Animate date-of-birth and age.
    // 6. Show 'X' icon when a field is focused.
    // 7. Disable phone number field when 'None' is selected.

    Observable
        .mergeArray(
            screenCreates(),
            formChanges())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .subscribe { uiChange -> uiChange(this) }

    fullNameEditText.showKeyboard()
    upButton.setOnClickListener { screenRouter.pop() }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun formChanges(): Observable<UiEvent> {
    return Observable.mergeArray(
        fullNameEditText.textChanges(::PatientFullNameTextChanged),
        phoneNumberEditText.textChanges(::PatientPhoneNumberTextChanged),
        dateOfBirthEditText.textChanges(::PatientDateOfBirthTextChanged),
        ageEditText.textChanges(::PatientAgeTextChanged),
        colonyOrVillageEditText.textChanges(::PatientColonyOrVillageTextChanged),
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
        .filter { it != -1 }
        .map { checkedId -> radioIdToGenders[checkedId] }
        .map { PatientGenderChanged(it) }
  }

  fun preFillFields(details: OngoingPatientEntry) {
    fullNameEditText.setTextAndCursor(details.personalDetails?.fullName)
    phoneNumberEditText.setTextAndCursor(details.phoneNumber?.number)
    dateOfBirthEditText.setTextAndCursor(details.personalDetails?.dateOfBirth)
    ageEditText.setTextAndCursor(details.personalDetails?.age)
    colonyOrVillageEditText.setTextAndCursor(details.address?.colonyOrVillage)
    districtEditText.setTextAndCursor(details.address?.district)
    stateEditText.setTextAndCursor(details.address?.state)
  }
}

private fun <T> EditText.textChanges(mapper: (String) -> T): Observable<T> {
  return RxTextView.textChanges(this)
      .map(CharSequence::toString)
      .map(mapper)
}
