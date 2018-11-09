package org.simple.clinic.newentry

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.transition.ChangeBounds
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.medicalhistory.newentry.NewMedicalHistoryScreen
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.patient.Gender.FEMALE
import org.simple.clinic.patient.Gender.MALE
import org.simple.clinic.patient.Gender.TRANSGENDER
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.scrollToChild
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class PatientEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientEntryScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientEntryScreenController

  @Inject
  lateinit var activityLifecycle: Observable<TheActivityLifecycle>

  private val backButton by bindView<View>(R.id.patiententry_back)
  private val formScrollView by bindView<ScrollView>(R.id.patiententry_form_scrollview)
  private val fullNameEditText by bindView<EditText>(R.id.patiententry_full_name)
  private val fullNameInputLayout by bindView<TextInputLayout>(R.id.patiententry_full_name_inputlayout)
  private val phoneNumberEditText by bindView<EditText>(R.id.patiententry_phone_number)
  private val phoneNumberInputLayout by bindView<TextInputLayout>(R.id.patiententry_phone_number_inputlayout)
  private val dateOfBirthEditText by bindView<DateOfBirthEditText>(R.id.patiententry_date_of_birth)
  private val dateOfBirthInputLayout by bindView<TextInputLayout>(R.id.patiententry_date_of_birth_inputlayout)
  private val dateOfBirthEditTextContainer by bindView<ViewGroup>(R.id.patiententry_date_of_birth_container)
  private val ageEditText by bindView<EditText>(R.id.patiententry_age)
  private val ageEditTextInputLayout by bindView<TextInputLayout>(R.id.patiententry_age_inputlayout)
  private val ageEditTextContainer by bindView<ViewGroup>(R.id.patiententry_age_container)
  private val dateOfBirthAndAgeSeparator by bindView<View>(R.id.patiententry_dateofbirth_and_age_separator)
  private val genderRadioGroup by bindView<RadioGroup>(R.id.patiententry_gender_radiogroup)
  private val femaleRadioButton by bindView<RadioButton>(R.id.patiententry_gender_female)
  private val maleRadioButton by bindView<RadioButton>(R.id.patiententry_gender_male)
  private val transgenderRadioButton by bindView<RadioButton>(R.id.patiententry_gender_transgender)
  private val genderErrorTextView by bindView<TextView>(R.id.patiententry_gender_validation_error)
  private val colonyOrVillageEditText by bindView<EditText>(R.id.patiententry_colony_or_village)
  private val colonyOrVillageInputLayout by bindView<TextInputLayout>(R.id.patiententry_colony_or_village_inputlayout)
  private val districtEditText by bindView<EditText>(R.id.patiententry_district)
  private val districtInputLayout by bindView<TextInputLayout>(R.id.patiententry_district_inputlayout)
  private val stateEditText by bindView<EditText>(R.id.patiententry_state)
  private val stateInputLayout by bindView<TextInputLayout>(R.id.patiententry_state_inputlayout)
  private val saveButtonFrame by bindView<PrimarySolidButtonWithFrame>(R.id.patiententry_save)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    fullNameEditText.showKeyboard()
    backButton.setOnClickListener { screenRouter.pop() }

    // Not sure why, but setting android:nextFocusDown in XML isn't working,
    // so doing this manually here.
    dateOfBirthEditText.imeOptions += EditorInfo.IME_ACTION_NEXT
    dateOfBirthEditText.setOnEditorActionListener { _, actionId, _ ->
      // When date is empty, this will move focus to age field and colony field otherwise.
      if (!dateOfBirthEditText.text!!.isBlank() && actionId == EditorInfo.IME_ACTION_NEXT) {
        colonyOrVillageEditText.requestFocus()
        true
      } else {
        false
      }
    }

    Observable
        .mergeArray(
            screenCreates(),
            screenPauses(),
            formChanges(),
            saveClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun screenPauses() = activityLifecycle.ofType<TheActivityLifecycle.Paused>()

  private fun formChanges(): Observable<UiEvent> {
    return Observable.mergeArray(
        fullNameEditText.textChanges(::PatientFullNameTextChanged),
        phoneNumberEditText.textChanges(::PatientPhoneNumberTextChanged),
        dateOfBirthEditText.textChanges(::PatientDateOfBirthTextChanged),
        dateOfBirthEditText.focusChanges.map(::PatientDateOfBirthFocusChanged),
        ageEditText.textChanges(::PatientAgeTextChanged),
        colonyOrVillageEditText.textChanges(::PatientColonyOrVillageTextChanged),
        districtEditText.textChanges(::PatientDistrictTextChanged),
        stateEditText.textChanges(::PatientStateTextChanged),
        genderChanges())
  }

  private fun genderChanges(): Observable<PatientGenderChanged> {
    val radioIdToGenders = mapOf(
        R.id.patiententry_gender_female to FEMALE,
        R.id.patiententry_gender_male to MALE,
        R.id.patiententry_gender_transgender to TRANSGENDER)

    return RxRadioGroup.checkedChanges(genderRadioGroup)
        .map { checkedId ->
          val gender = radioIdToGenders[checkedId]
          PatientGenderChanged(gender.toOptional())
        }
  }

  private fun saveClicks(): Observable<UiEvent> {
    val stateImeClicks = RxTextView.editorActions(stateEditText) { it == EditorInfo.IME_ACTION_DONE }

    return RxView.clicks(saveButtonFrame.button)
        .mergeWith(stateImeClicks)
        .map { PatientEntrySaveClicked() }
  }

  fun preFillFields(entry: OngoingNewPatientEntry) {
    fullNameEditText.setTextAndCursor(entry.personalDetails?.fullName)
    phoneNumberEditText.setTextAndCursor(entry.phoneNumber?.number)
    dateOfBirthEditText.setTextAndCursor(entry.personalDetails?.dateOfBirth)
    ageEditText.setTextAndCursor(entry.personalDetails?.age)
    colonyOrVillageEditText.setTextAndCursor(entry.address?.colonyOrVillage)
    districtEditText.setTextAndCursor(entry.address?.district)
    stateEditText.setTextAndCursor(entry.address?.state)

    entry.personalDetails?.gender?.let {
      val genderButton = when (it) {
        MALE -> maleRadioButton
        FEMALE -> femaleRadioButton
        TRANSGENDER -> transgenderRadioButton
      }
      genderButton.isChecked = true
    }
  }

  fun openMedicalHistoryEntryScreen() {
    screenRouter.push(NewMedicalHistoryScreen.KEY)
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
      DATE_OF_BIRTH_VISIBLE, BOTH_VISIBLE -> View.VISIBLE
      else -> View.GONE
    }

    dateOfBirthAndAgeSeparator.visibility = when (visibility) {
      BOTH_VISIBLE -> View.VISIBLE
      else -> View.GONE
    }

    ageEditTextContainer.visibility = when (visibility) {
      DateOfBirthAndAgeVisibility.AGE_VISIBLE, BOTH_VISIBLE -> View.VISIBLE
      else -> View.GONE
    }
  }

  fun setShowDatePatternInDateOfBirthLabel(showPattern: Boolean) {
    val labelRes = when (showPattern) {
      true -> R.string.patiententry_date_of_birth_focused
      false -> R.string.patiententry_date_of_birth_unfocused
    }
    dateOfBirthInputLayout.hint = resources.getString(labelRes)
  }

  fun showEmptyFullNameError(show: Boolean) {
    if (show) {
      fullNameInputLayout.error = resources.getString(R.string.patiententry_error_empty_fullname)
    } else {
      fullNameInputLayout.error = null
    }
  }

  fun showLengthTooShortPhoneNumberError(show: Boolean) {
    if (show) {
      phoneNumberInputLayout.error = context.getString(R.string.patiententry_error_phonenumber_length_less)
    } else {
      phoneNumberInputLayout.error = null
    }
  }

  fun showLengthTooLongPhoneNumberError(show: Boolean) {
    if (show) {
      phoneNumberInputLayout.error = context.getString(R.string.patiententry_error_phonenumber_length_more)
    } else {
      phoneNumberInputLayout.error = null
    }
  }

  fun showMissingGenderError(show: Boolean) {
    if (show) {
      genderErrorTextView.visibility = View.VISIBLE
    } else {
      genderErrorTextView.visibility = View.GONE
    }
  }

  fun showEmptyColonyOrVillageError(show: Boolean) {
    colonyOrVillageInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_empty_colony_or_village)
      else -> null
    }
  }

  fun showEmptyDistrictError(show: Boolean) {
    districtInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_empty_district)
      else -> null
    }
  }

  fun showEmptyStateError(show: Boolean) {
    stateInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_state_empty)
      else -> null
    }
  }

  fun showEmptyDateOfBirthAndAgeError(show: Boolean) {
    ageEditTextInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_both_dateofbirth_and_age_empty)
      else -> null
    }
  }

  fun showInvalidDateOfBirthError(show: Boolean) {
    dateOfBirthInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_invalid_dateofbirth)
      else -> null
    }
  }

  fun showDateOfBirthIsInFutureError(show: Boolean) {
    dateOfBirthInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_dateofbirth_is_in_future)
      else -> null
    }
  }

  fun scrollToFirstFieldWithError() {
    val views = arrayOf(
        fullNameInputLayout,
        phoneNumberInputLayout,
        ageEditTextInputLayout,
        dateOfBirthInputLayout,
        genderErrorTextView,
        colonyOrVillageInputLayout,
        districtInputLayout,
        stateInputLayout)

    val isGenderErrorView: (View) -> Boolean = {
      it.id == R.id.patiententry_gender_validation_error
    }

    val firstFieldWithError = views
        .filter {
          when {
            isGenderErrorView(it) -> it.visibility == View.VISIBLE
            it is TextInputLayout -> it.error.isNullOrBlank().not()
            else -> throw AssertionError()
          }
        }
        .map {
          when {
            isGenderErrorView(it) -> genderRadioGroup
            else -> it
          }
        }
        .first()

    formScrollView.scrollToChild(firstFieldWithError, onScrollComplete = { firstFieldWithError.requestFocus() })
  }

  fun scrollFormToBottom() {
    formScrollView.post {
      formScrollView.smoothScrollTo(0, formScrollView.height)
    }
  }
}

private fun <T> EditText.textChanges(mapper: (String) -> T): Observable<T> {
  return RxTextView.textChanges(this)
      .map { it.toString() }
      .map(mapper)
}
