package org.simple.clinic.editpatient

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.editpatient.PatientEditValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.editpatient.PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.editpatient.PatientEditValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.PatientEditValidationError.STATE_EMPTY
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Gender.Female
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender
import org.simple.clinic.patient.Gender.Unknown
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthEditText
import org.simple.clinic.widgets.scrollToChild
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.textChanges
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class PatientEditScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

  @Inject
  lateinit var controller: PatientEditScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  @field:[Inject Named("date_for_user_input")]
  lateinit var dateOfBirthFormat: DateTimeFormatter

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var crashReporter: CrashReporter

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
  private val ageEditext by bindView<EditText>(R.id.patientedit_age)
  private val dateOfBirthEditText by bindView<DateOfBirthEditText>(R.id.patientedit_date_of_birth)
  private val dateOfBirthInputLayout by bindView<TextInputLayout>(R.id.patientedit_date_of_birth_inputlayout)
  private val dateOfBirthEditTextContainer by bindView<ViewGroup>(R.id.patientedit_date_of_birth_container)
  private val dateOfBirthAndAgeSeparator by bindView<View>(R.id.patientedit_dateofbirth_and_age_separator)
  private val ageEditTextContainer by bindView<ViewGroup>(R.id.patientedit_age_container)
  private val backButton by bindView<ImageButton>(R.id.patientedit_back)
  private val saveButton by bindView<PrimarySolidButtonWithFrame>(R.id.patientedit_save)
  private val ageInputLayout by bindView<TextInputLayout>(R.id.patientedit_age_inputlayout)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            screenCreates(),
            saveClicks(),
            nameTextChanges(),
            phoneNumberTextChanges(),
            districtTextChanges(),
            stateTextChanges(),
            colonyTextChanges(),
            genderChanges(),
            dateOfBirthTextChanges(),
            dateOfBirthFocusChanges(),
            ageTextChanges(),
            backClicks()
        ),
        controller = controller,
        screenDestroys = screenDestroys
    )
  }

  private fun screenCreates(): Observable<UiEvent> {
    val key = screenRouter.key<PatientEditScreenKey>(this)

    return Observable.just(PatientEditScreenCreated.fromPatientUuid(key.patientUuid))
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

  private fun backClicks(): Observable<UiEvent> {
    val hardwareBackKeyClicks = Observable.create<Any> { emitter ->
      val interceptor = object : BackPressInterceptor {
        override fun onInterceptBackPress(callback: BackPressInterceptCallback) {
          emitter.onNext(Any())
          callback.markBackPressIntercepted()
        }
      }
      emitter.setCancellable { screenRouter.unregisterBackPressInterceptor(interceptor) }
      screenRouter.registerBackPressInterceptor(interceptor)
    }

    return RxView.clicks(backButton)
        .mergeWith(hardwareBackKeyClicks)
        .map { PatientEditBackClicked() }
  }

  private fun genderChanges(): Observable<UiEvent> {
    val radioIdToGenders = mapOf(
        R.id.patientedit_gender_female to Female,
        R.id.patientedit_gender_male to Male,
        R.id.patientedit_gender_transgender to Transgender)

    return RxRadioGroup.checkedChanges(genderRadioGroup)
        .filter { it != -1 }
        .map { checkedId ->
          val gender = radioIdToGenders[checkedId]!!
          PatientEditGenderChanged(gender)
        }
  }

  private fun dateOfBirthTextChanges(): Observable<UiEvent> = dateOfBirthEditText.textChanges(::PatientEditDateOfBirthTextChanged)

  private fun dateOfBirthFocusChanges(): Observable<UiEvent> = dateOfBirthEditText.focusChanges.map(::PatientEditDateOfBirthFocusChanged)

  private fun ageTextChanges(): Observable<UiEvent> = ageEditext.textChanges(::PatientEditAgeTextChanged)

  fun setPatientName(name: String) {
    fullNameEditText.setTextAndCursor(name)
  }

  fun setPatientPhoneNumber(number: String) {
    phoneNumberEditText.setTextAndCursor(number)
  }

  fun setColonyOrVillage(colonyOrVillage: String) {
    colonyEditText.setTextAndCursor(colonyOrVillage)
  }

  fun setDistrict(district: String) {
    districtEditText.setTextAndCursor(district)
  }

  fun setState(state: String) {
    stateEditText.setTextAndCursor(state)
  }

  fun setGender(gender: Gender) {
    val genderButton: RadioButton? = when (gender) {
      Male -> maleRadioButton
      Female -> femaleRadioButton
      Transgender -> transgenderRadioButton
      is Unknown -> {
        crashReporter.report(IllegalStateException("Heads-up: unknown gender ${gender.actualValue} found in ${PatientEditScreen::class.java.name}"))
        null
      }
    }

    genderButton?.isChecked = true
  }

  fun setPatientAge(age: Int) {
    ageEditext.setTextAndCursor(age.toString())
  }

  fun setPatientDateofBirth(dateOfBirth: LocalDate) {
    dateOfBirthEditText.setTextAndCursor(dateOfBirthFormat.format(dateOfBirth))
  }

  fun showValidationErrors(errors: Set<PatientEditValidationError>) {
    errors.forEach {
      when (it) {
        FULL_NAME_EMPTY -> {
          showEmptyFullNameError(true)
        }

        PHONE_NUMBER_EMPTY,
        PHONE_NUMBER_LENGTH_TOO_SHORT -> {
          showLengthTooShortPhoneNumberError()
        }

        PHONE_NUMBER_LENGTH_TOO_LONG -> {
          showLengthTooLongPhoneNumberError()
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

        BOTH_DATEOFBIRTH_AND_AGE_ABSENT -> {
          showAgeEmptyError(true)
        }

        INVALID_DATE_OF_BIRTH -> {
          showInvalidaDateOfBithError()
        }

        DATE_OF_BIRTH_IN_FUTURE -> {
          showDateOfBirthIsInFutureError()
        }
      }.exhaustive()
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
          hidePhoneNumberError()
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

        BOTH_DATEOFBIRTH_AND_AGE_ABSENT -> {
          showAgeEmptyError(false)
        }

        INVALID_DATE_OF_BIRTH, DATE_OF_BIRTH_IN_FUTURE -> {
          hideDateOfBirthError()
        }
      }.exhaustive()
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
    fullNameInputLayout.error = when {
      show -> resources.getString(R.string.patientedit_error_empty_fullname)
      else -> null
    }
  }

  private fun showAgeEmptyError(show: Boolean) {
    ageInputLayout.error = when {
      show -> resources.getString(R.string.patientedit_error_both_dateofbirth_and_age_empty)
      else -> null
    }
  }

  private fun showLengthTooShortPhoneNumberError() {
    phoneNumberInputLayout.error = context.getString(R.string.patientedit_error_phonenumber_length_less)
  }

  private fun showLengthTooLongPhoneNumberError() {
    phoneNumberInputLayout.error = context.getString(R.string.patientedit_error_phonenumber_length_more)
  }

  private fun showInvalidaDateOfBithError() {
    dateOfBirthInputLayout.error = context.getString(R.string.patientedit_error_invalid_dateofbirth)
  }

  private fun showDateOfBirthIsInFutureError() {
    dateOfBirthInputLayout.error = context.getString(R.string.patientedit_error_dateofbirth_is_in_future)
  }

  private fun hideDateOfBirthError() {
    dateOfBirthInputLayout.error = null
  }

  private fun hidePhoneNumberError() {
    phoneNumberInputLayout.error = null
  }

  fun scrollToFirstFieldWithError() {
    val views = arrayOf(
        fullNameInputLayout,
        phoneNumberInputLayout,
        colonyOrVillageInputLayout,
        districtInputLayout,
        stateInputLayout,
        ageInputLayout,
        dateOfBirthInputLayout)

    val firstFieldWithError = views.firstOrNull { it.error.isNullOrBlank().not() }

    firstFieldWithError?.let {
      formScrollView.scrollToChild(it, onScrollComplete = { it.requestFocus() })
    }
  }

  fun goBack() {
    screenRouter.pop()
  }

  fun showDatePatternInDateOfBirthLabel() {
    dateOfBirthInputLayout.hint = resources.getString(R.string.patientedit_date_of_birth_focused)
  }

  fun hideDatePatternInDateOfBirthLabel() {
    dateOfBirthInputLayout.hint = resources.getString(R.string.patientedit_date_of_birth_unfocused)
  }

  fun setDateOfBirthAndAgeVisibility(visibility: DateOfBirthAndAgeVisibility) {
    val transition = TransitionSet()
        .addTransition(ChangeBounds())
        .addTransition(Fade())
        .setOrdering(TransitionSet.ORDERING_TOGETHER)
        .setDuration(250)
        .setInterpolator(FastOutSlowInInterpolator())
    TransitionManager.beginDelayedTransition(this, transition)

    dateOfBirthEditTextContainer.visibility = when (visibility) {
      DATE_OF_BIRTH_VISIBLE, BOTH_VISIBLE -> VISIBLE
      else -> GONE
    }

    dateOfBirthAndAgeSeparator.visibility = when (visibility) {
      BOTH_VISIBLE -> VISIBLE
      else -> GONE
    }

    ageEditTextContainer.visibility = when (visibility) {
      AGE_VISIBLE, BOTH_VISIBLE -> VISIBLE
      else -> GONE
    }
  }

  fun showDiscardChangesAlert() {
    ConfirmDiscardChangesDialog.show(activity.supportFragmentManager)
  }
}
