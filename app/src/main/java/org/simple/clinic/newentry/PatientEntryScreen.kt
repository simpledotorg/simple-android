package org.simple.clinic.newentry

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import android.widget.RelativeLayout
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
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_manual_patient_entry.view.*
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.medicalhistory.newentry.NewMedicalHistoryScreenKey
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Gender.Female
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender
import org.simple.clinic.patient.Gender.Unknown
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.widgets.scrollToChild
import org.simple.clinic.widgets.setCompoundDrawableStartWithTint
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import org.simple.clinic.widgets.textChanges
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class PatientEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientEntryScreenController

  @Inject
  lateinit var activityLifecycle: Observable<TheActivityLifecycle>

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  @Inject
  lateinit var crashReporter: CrashReporter

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    phoneNumberEditText.showKeyboard()
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

    // Compound drawable tinting is only supported in API23+. AppCompatTextView does not have
    // support for compound drawable tinting either, so we need to do this in code.
    identifierTextView.setCompoundDrawableStartWithTint(R.drawable.patient_id_card, R.color.grey1)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            screenPauses(),
            formChanges(),
            saveClicks()
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
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
        R.id.femaleRadioButton to Female,
        R.id.maleRadioButton to Male,
        R.id.transgenderRadioButton to Transgender)

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
    entry.personalDetails?.gender?.let(this::prefillGender)
    entry.identifier?.let(this::prefillIdentifier)
  }

  private fun prefillIdentifier(identifier: Identifier) {
    val identifierDisplayString = identifierDisplayAdapter.valueAsText(identifier)
    identifierTextView.text = identifierDisplayString
  }

  private fun prefillGender(gender: Gender) {
    val genderButton: RadioButton? = when (gender) {
      Male -> maleRadioButton
      Female -> femaleRadioButton
      Transgender -> transgenderRadioButton
      is Unknown -> {
        crashReporter.report(IllegalStateException("Heads-up: unknown gender ${gender.actualValue} found in ${PatientEntryScreen::class.java.name}"))
        null
      }
    }

    genderButton?.isChecked = true
  }

  fun openMedicalHistoryEntryScreen() {
    screenRouter.push(NewMedicalHistoryScreenKey())
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
      it.id == R.id.genderErrorTextView
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

  fun showIdentifierSection() {
    identifierContainer.visibleOrGone(isVisible = true)
  }

  fun hideIdentifierSection() {
    identifierContainer.visibleOrGone(isVisible = false)
  }
}
