package org.simple.clinic.newentry

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface.BOLD
import android.os.Parcelable
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.f2prateek.rx.preferences2.Preference
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_manual_patient_entry.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.main.TheActivity
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.newentry.NewMedicalHistoryScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Gender.Female
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender
import org.simple.clinic.patient.Gender.Unknown
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.ReminderConsent.Denied
import org.simple.clinic.patient.ReminderConsent.Granted
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Truss
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.scrollToChild
import org.simple.clinic.widgets.setCompoundDrawableStartWithTint
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import org.simple.clinic.widgets.textChanges
import org.simple.clinic.widgets.topRelativeTo
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject
import javax.inject.Named

class PatientEntryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), PatientEntryUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @field:[Inject Named("number_of_patients_registered")]
  lateinit var patientRegisteredCount: Preference<Int>

  @Inject
  lateinit var phoneNumberValidator: PhoneNumberValidator

  @Inject
  lateinit var dobValidator: UserInputDateValidator

  private val allTextInputFields: List<EditText> by unsafeLazy {
    listOf(
        fullNameEditText,
        phoneNumberEditText,
        ageEditText,
        dateOfBirthEditText,
        colonyOrVillageEditText,
        districtEditText,
        stateEditText
    )
  }

  /**
   * We show the keyboard for the first empty text field when we prefill the text fields. However,
   * this should only be done the first time the prefill happens and not afterwards. This is used
   * to track the fact that we've already shown it.
   **/
  private var alreadyFocusedOnEmptyTextField: Boolean = false

  private val viewRenderer = PatientEntryViewRenderer(this)

  private val events: Observable<PatientEntryEvent> by unsafeLazy {
    Observable
        .merge(formChanges(), saveClicks(), consentChanges())
        .compose(ReportAnalyticsEvents())
        .cast<PatientEntryEvent>()
  }

  private val delegate by unsafeLazy {
    val effectHandler = PatientEntryEffectHandler.create(
        userSession,
        facilityRepository,
        patientRepository,
        patientRegisteredCount,
        this,
        schedulersProvider
    )

    MobiusDelegate(
        events.ofType(),
        PatientEntryModel.DEFAULT,
        PatientEntryInit(),
        PatientEntryUpdate(phoneNumberValidator, dobValidator),
        effectHandler,
        viewRenderer::render,
        crashReporter
    )
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

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

    setConsentText()

    delegate.prepare()
  }

  private fun setConsentText() {
    val consentText = Truss()
        .pushSpan(StyleSpan(BOLD))
        .append(resources.getString(R.string.patiententry_consent_header))
        .popSpan()
        .append(resources.getString(R.string.patiententry_consent_first_para))
        .append("\n\n")
        .append(resources.getString(R.string.patiententry_consent_second_para))
        .build()
    consentTextView.text = consentText
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    val viewState = delegate.onRestoreInstanceState(state)
    super.onRestoreInstanceState(viewState)
  }

  private fun formChanges(): Observable<PatientEntryEvent> {
    return Observable.mergeArray(
        fullNameEditText.textChanges(::FullNameChanged),
        phoneNumberEditText.textChanges(::PhoneNumberChanged),
        dateOfBirthEditText.textChanges(::DateOfBirthChanged),
        dateOfBirthEditText.focusChanges.map(::DateOfBirthFocusChanged),
        ageEditText.textChanges(::AgeChanged),
        colonyOrVillageEditText.textChanges(::ColonyOrVillageChanged),
        districtEditText.textChanges(::DistrictChanged),
        stateEditText.textChanges(::StateChanged),
        genderChanges())
  }

  private fun genderChanges(): Observable<GenderChanged> {
    val radioIdToGenders = mapOf(
        R.id.femaleRadioButton to Female,
        R.id.maleRadioButton to Male,
        R.id.transgenderRadioButton to Transgender)

    return RxRadioGroup.checkedChanges(genderRadioGroup)
        .map { checkedId ->
          val gender = radioIdToGenders[checkedId]
          GenderChanged(gender.toOptional())
        }
  }

  private fun saveClicks(): Observable<PatientEntryEvent> {
    val stateImeClicks = RxTextView.editorActions(stateEditText) { it == EditorInfo.IME_ACTION_DONE }

    return RxView.clicks(saveButtonFrame.button)
        .mergeWith(stateImeClicks)
        .map { SaveClicked }
  }

  private fun consentChanges(): Observable<PatientEntryEvent> =
      RxCompoundButton.checkedChanges(consentSwitch)
          .map { checked -> if (checked) Granted else Denied }
          .map(::ReminderConsentChanged)

  override fun preFillFields(entry: OngoingNewPatientEntry) {
    fullNameEditText.setTextAndCursor(entry.personalDetails?.fullName)
    phoneNumberEditText.setTextAndCursor(entry.phoneNumber?.number)
    dateOfBirthEditText.setTextAndCursor(entry.personalDetails?.dateOfBirth)
    ageEditText.setTextAndCursor(entry.personalDetails?.age)
    colonyOrVillageEditText.setTextAndCursor(entry.address?.colonyOrVillage)
    districtEditText.setTextAndCursor(entry.address?.district)
    stateEditText.setTextAndCursor(entry.address?.state)
    entry.personalDetails?.gender?.let(this::prefillGender)
    entry.identifier?.let(this::prefillIdentifier)

    showKeyboardForFirstEmptyTextField()
  }

  private fun showKeyboardForFirstEmptyTextField() {
    if (!alreadyFocusedOnEmptyTextField) {
      val firstEmptyTextField = allTextInputFields.firstOrNull { it.text.isNullOrBlank() }
      firstEmptyTextField?.showKeyboard()
      alreadyFocusedOnEmptyTextField = true
    }
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

  override fun openMedicalHistoryEntryScreen() {
    screenRouter.push(NewMedicalHistoryScreenKey())
  }

  override fun setDateOfBirthAndAgeVisibility(visibility: DateOfBirthAndAgeVisibility) {
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

  override fun setShowDatePatternInDateOfBirthLabel(showPattern: Boolean) {
    val labelRes = when (showPattern) {
      true -> R.string.patiententry_date_of_birth_focused
      false -> R.string.patiententry_date_of_birth_unfocused
    }
    dateOfBirthInputLayout.hint = resources.getString(labelRes)
  }

  override fun showEmptyFullNameError(show: Boolean) {
    if (show) {
      fullNameInputLayout.error = resources.getString(R.string.patiententry_error_empty_fullname)
    } else {
      fullNameInputLayout.error = null
    }
  }

  override fun showLengthTooShortPhoneNumberError(show: Boolean) {
    if (show) {
      phoneNumberInputLayout.error = context.getString(R.string.patiententry_error_phonenumber_length_less)
    } else {
      phoneNumberInputLayout.error = null
    }
  }

  override fun showLengthTooLongPhoneNumberError(show: Boolean) {
    if (show) {
      phoneNumberInputLayout.error = context.getString(R.string.patiententry_error_phonenumber_length_more)
    } else {
      phoneNumberInputLayout.error = null
    }
  }

  override fun showMissingGenderError(show: Boolean) {
    if (show) {
      genderErrorTextView.visibility = View.VISIBLE
    } else {
      genderErrorTextView.visibility = View.GONE
    }
  }

  override fun showEmptyColonyOrVillageError(show: Boolean) {
    colonyOrVillageInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_empty_colony_or_village)
      else -> null
    }
  }

  override fun showEmptyDistrictError(show: Boolean) {
    districtInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_empty_district)
      else -> null
    }
  }

  override fun showEmptyStateError(show: Boolean) {
    stateInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_state_empty)
      else -> null
    }
  }

  override fun showEmptyDateOfBirthAndAgeError(show: Boolean) {
    ageEditTextInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_both_dateofbirth_and_age_empty)
      else -> null
    }
  }

  override fun showInvalidDateOfBirthError(show: Boolean) {
    dateOfBirthInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_invalid_dateofbirth)
      else -> null
    }
  }

  override fun showDateOfBirthIsInFutureError(show: Boolean) {
    dateOfBirthInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_error_dateofbirth_is_in_future)
      else -> null
    }
  }

  override fun scrollToFirstFieldWithError() {
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

  override fun scrollFormToBottom() {
    formScrollView.post {
      formScrollView.smoothScrollTo(0, ageEditText.topRelativeTo(patientEntryRoot))
    }
  }

  override fun showIdentifierSection() {
    identifierContainer.visibleOrGone(isVisible = true)
  }

  override fun hideIdentifierSection() {
    identifierContainer.visibleOrGone(isVisible = false)
  }
}
