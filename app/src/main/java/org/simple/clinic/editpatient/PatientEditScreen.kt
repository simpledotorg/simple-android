package org.simple.clinic.editpatient

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.util.AttributeSet
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.editpatient.PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.PatientEditValidationError.STATE_EMPTY
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Gender.FEMALE
import org.simple.clinic.patient.Gender.MALE
import org.simple.clinic.patient.Gender.TRANSGENDER
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.scrollToChild
import javax.inject.Inject

class PatientEditScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

  companion object {
    @JvmField
    val KEY = ::PatientEditScreenKey
  }

  @Inject
  lateinit var controller: PatientEditScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val formScrollView by bindView<ScrollView>(R.id.patientedit_form_scrollview)
  private val fullNameEditText by bindView<EditText>(R.id.patientedit_full_name)
  private val fullNameInputLayout by bindView<TextInputLayout>(R.id.patientedit_full_name_inputlayout)
  private val phoneNumberEditText by bindView<EditText>(R.id.patientedit_phone_number)
  private val phoneNumberInputLayout by bindView<TextInputLayout>(R.id.patientedit_phone_number_inputlayout)
  private val colonyEditText by bindView<EditText>(R.id.patientedit_colony_or_village)
  private val colonyOrVillageInputLayout by bindView<TextInputLayout>(R.id.patientedit_colony_or_village_inputlayout)
  private val districtEditText by bindView<EditText>(R.id.patientedit_district)
  private val districtInputLayout by bindView<TextInputLayout>(R.id.patientedit_district_inputlayout)
  private val stateEditText by bindView<EditText>(R.id.patientedit_state)
  private val stateInputLayout by bindView<TextInputLayout>(R.id.patientedit_state_inputlayout)
  private val femaleRadioButton by bindView<RadioButton>(R.id.patientedit_gender_female)
  private val maleRadioButton by bindView<RadioButton>(R.id.patientedit_gender_male)
  private val transgenderRadioButton by bindView<RadioButton>(R.id.patientedit_gender_transgender)
  private val genderRadioGroup by bindView<RadioGroup>(R.id.patientedit_gender_radiogroup)

  private val saveButton by bindView<PrimarySolidButtonWithFrame>(R.id.patientedit_save)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    Observable
        .mergeArray(
            screenCreates(),
            saveClicks(),
            nameTextChanges(),
            phoneNumberTextChanges(),
            districtTextChanges(),
            stateTextChanges(),
            colonyTextChanges(),
            genderChanges()
        )
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates(): Observable<UiEvent> {
    val key = screenRouter.key<PatientEditScreenKey>(this)

    return Observable.just(PatientEditScreenCreated(key.patientUuid))
  }

  private fun saveClicks(): Observable<UiEvent> {
    return RxView.clicks(saveButton.button).map { PatientEditSaveClicked() }
  }

  private fun nameTextChanges(): Observable<UiEvent> {
    return RxTextView.textChanges(fullNameEditText).map { PatientEditPatientNameTextChanged(it.toString()) }
  }

  private fun phoneNumberTextChanges(): Observable<UiEvent> {
    return RxTextView.textChanges(phoneNumberEditText).map { PatientEditPhoneNumberTextChanged(it.toString()) }
  }

  private fun districtTextChanges(): Observable<UiEvent> {
    return RxTextView.textChanges(districtEditText).map { PatientEditDistrictTextChanged(it.toString()) }
  }

  private fun stateTextChanges(): Observable<UiEvent> {
    return RxTextView.textChanges(stateEditText).map { PatientEditStateTextChanged(it.toString()) }
  }

  private fun colonyTextChanges(): Observable<UiEvent> {
    return RxTextView.textChanges(colonyEditText).map { PatientEditColonyOrVillageChanged(it.toString()) }
  }

  private fun genderChanges(): Observable<UiEvent> {
    val radioIdToGenders = mapOf(
        R.id.patientedit_gender_female to FEMALE,
        R.id.patientedit_gender_male to MALE,
        R.id.patientedit_gender_transgender to TRANSGENDER)

    return RxRadioGroup.checkedChanges(genderRadioGroup)
        .filter { it != -1 }
        .map { checkedId ->
          val gender = radioIdToGenders[checkedId]!!
          PatientEditGenderChanged(gender)
        }
  }

  fun setPatientName(name: String) {
    fullNameEditText.setText(name)
  }

  fun setPatientPhoneNumber(number: String) {
    phoneNumberEditText.setText(number)
  }

  fun setColonyOrVillage(colonyOrVillage: String) {
    colonyEditText.setText(colonyOrVillage)
  }

  fun setDistrict(district: String) {
    districtEditText.setText(district)
  }

  fun setState(state: String) {
    stateEditText.setText(state)
  }

  fun setGender(gender: Gender) {
    val genderButton = when (gender) {
      MALE -> maleRadioButton
      FEMALE -> femaleRadioButton
      TRANSGENDER -> transgenderRadioButton
    }

    genderButton.isChecked = true
  }

  fun showValidationErrors(errors: Set<PatientEditValidationError>) {
    errors.forEach {
      when (it) {
        FULL_NAME_EMPTY -> {
          showEmptyFullNameError(true)
        }

        PHONE_NUMBER_EMPTY,
        PHONE_NUMBER_LENGTH_TOO_SHORT -> {
          showLengthTooShortPhoneNumberError(true)
        }

        PHONE_NUMBER_LENGTH_TOO_LONG -> {
          showLengthTooLongPhoneNumberError(true)
        }

        COLONY_OR_VILLAGE_EMPTY -> {
          showEmptyColonyOrVillageError(true)
        }

        DISTRICT_EMPTY -> {
          showEmptyDistrictError(true)
        }

        STATE_EMPTY -> {
          showEmptyStateError(true)
        }
      }
    }
  }

  fun hideValidationErrors(errors: Set<PatientEditValidationError>) {
    errors.forEach {
      when (it) {
        FULL_NAME_EMPTY -> {
          showEmptyFullNameError(false)
        }

        PHONE_NUMBER_EMPTY,
        PHONE_NUMBER_LENGTH_TOO_SHORT,
        PHONE_NUMBER_LENGTH_TOO_LONG -> {
          showLengthTooShortPhoneNumberError(false)
        }

        COLONY_OR_VILLAGE_EMPTY -> {
          showEmptyColonyOrVillageError(false)
        }

        DISTRICT_EMPTY -> {
          showEmptyDistrictError(false)
        }

        STATE_EMPTY -> {
          showEmptyStateError(false)
        }
      }
    }
  }

  private fun showEmptyColonyOrVillageError(showError: Boolean) {
    colonyOrVillageInputLayout.error = when {
      showError -> resources.getString(R.string.patientedit_error_empty_colony_or_village)
      else -> null
    }
  }

  private fun showEmptyDistrictError(show: Boolean) {
    districtInputLayout.error = when {
      show -> resources.getString(R.string.patientedit_error_empty_district)
      else -> null
    }
  }

  private fun showEmptyStateError(show: Boolean) {
    stateInputLayout.error = when {
      show -> resources.getString(R.string.patientedit_error_state_empty)
      else -> null
    }
  }

  private fun showEmptyFullNameError(show: Boolean) {
    if (show) {
      fullNameInputLayout.error = resources.getString(R.string.patientedit_error_empty_fullname)
    } else {
      fullNameInputLayout.error = null
    }
  }

  private fun showLengthTooShortPhoneNumberError(show: Boolean) {
    if (show) {
      phoneNumberInputLayout.error = context.getString(R.string.patientedit_error_phonenumber_length_less)
    } else {
      phoneNumberInputLayout.error = null
    }
  }

  private fun showLengthTooLongPhoneNumberError(show: Boolean) {
    if (show) {
      phoneNumberInputLayout.error = context.getString(R.string.patientedit_error_phonenumber_length_more)
    } else {
      phoneNumberInputLayout.error = null
    }
  }

  fun scrollToFirstFieldWithError() {
    val views = arrayOf(
        fullNameInputLayout,
        phoneNumberInputLayout,
        colonyOrVillageInputLayout,
        districtInputLayout,
        stateInputLayout)


    views
        .firstOrNull {
          it.error.isNullOrBlank().not()
        }
        ?.let { firstFieldWithError ->
          formScrollView.scrollToChild(firstFieldWithError, onScrollComplete = { firstFieldWithError.requestFocus() })
        }
  }

  fun goBack() {
    screenRouter.pop()
  }
}
