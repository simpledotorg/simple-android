package org.resolvetosavelives.red.newentry

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.transition.ChangeBounds
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
import org.resolvetosavelives.red.newentry.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.resolvetosavelives.red.newentry.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.resolvetosavelives.red.patient.Gender
import org.resolvetosavelives.red.patient.OngoingPatientEntry
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.util.toOptional
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
  private val dateOfBirthInputLayout by bindView<TextInputLayout>(R.id.patiententry_date_of_birth_inputlayout)
  private val dateOfBirthEditTextContainer by bindView<ViewGroup>(R.id.patiententry_date_of_birth_container)
  private val ageEditText by bindView<EditText>(R.id.patiententry_age)
  private val ageEditTextContainer by bindView<ViewGroup>(R.id.patiententry_age_container)
  private val dateOfBirthAndAgeSeparator by bindView<View>(R.id.patiententry_dateofbirth_and_age_separator)
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
    // - Show 'X' icon when a field is focused.
    // - Switch between 'date of birth' and 'date of birth (dd/mm/yyyy)` for DOB's hint.

    fullNameEditText.showKeyboard()
    upButton.setOnClickListener { screenRouter.pop() }

    // Save button is also disabled by the controller as soon as it starts emitting
    // UiChanges, but by the time that happens, the save button is visible on the
    // screen for a moment. Disabling it here solves the problem.
    setSaveButtonEnabled(false)

    // Not sure why, but setting android:nextFocusDown in XML isn't working,
    // so doing this manually here.
    dateOfBirthEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
    dateOfBirthEditText.setOnEditorActionListener({ _, actionId, _ ->
      // When date is empty, this will move focus to age field and colony field otherwise.
      if (!dateOfBirthEditText.text.isBlank() && actionId == EditorInfo.IME_ACTION_NEXT) {
        colonyOrVillageEditText.requestFocus()
        true
      } else {
        false
      }
    })

    Observable
        .mergeArray(
            screenCreates(),
            formChanges(),
            saveClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun formChanges(): Observable<UiEvent> {
    return Observable.mergeArray(
        fullNameEditText.textChanges(::PatientFullNameTextChanged),
        phoneNumberEditText.textChanges(::PatientPhoneNumberTextChanged),
        RxCompoundButton.checkedChanges(noPhoneNumberCheckBox).map(::PatientNoPhoneNumberToggled),
        dateOfBirthEditText.textChanges(::PatientDateOfBirthTextChanged),
        RxView.focusChanges(dateOfBirthEditText).map(::PatientDateOfBirthFocusChanged),
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
          PatientGenderChanged(gender.toOptional())
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

  fun setNoPhoneNumberCheckboxVisible(visible: Boolean) {
    TransitionManager.beginDelayedTransition(this, Fade()
        .addTarget(noPhoneNumberCheckBox)
        .setDuration(100)
        .setInterpolator(FastOutSlowInInterpolator()))

    noPhoneNumberCheckBox.visibility = when (visible) {
      true -> View.VISIBLE
      else -> View.GONE
    }
    if (!visible) {
      noPhoneNumberCheckBox.isChecked = visible
    }
  }

  fun setNoVillageOrColonyCheckboxVisible(visible: Boolean) {
    TransitionManager.beginDelayedTransition(this, Fade()
        .addTarget(noColonyOrVillageCheckBox)
        .setDuration(100)
        .setInterpolator(FastOutSlowInInterpolator()))

    noColonyOrVillageCheckBox.visibility = when (visible) {
      true -> View.VISIBLE
      else -> View.GONE
    }
    if (!visible) {
      noColonyOrVillageCheckBox.isChecked = visible
    }
  }
}

private fun <T> EditText.textChanges(mapper: (String) -> T): Observable<T> {
  return RxTextView.textChanges(this)
      .map { it.toString() }
      .map(mapper)
}
