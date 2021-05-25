package org.simple.clinic.newentry

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface.BOLD
import android.os.Parcelable
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.editorActions
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.databinding.ScreenManualPatientEntryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.medicalhistory.newentry.NewMedicalHistoryScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.form.AlternativeIdInputField
import org.simple.clinic.newentry.form.DistrictField
import org.simple.clinic.newentry.form.GenderField
import org.simple.clinic.newentry.form.LandlineOrMobileField
import org.simple.clinic.newentry.form.PatientNameField
import org.simple.clinic.newentry.form.StateField
import org.simple.clinic.newentry.form.StreetAddressField
import org.simple.clinic.newentry.form.VillageOrColonyField
import org.simple.clinic.newentry.form.ZoneField
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Gender.Female
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender
import org.simple.clinic.patient.Gender.Unknown
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.ReminderConsent.Denied
import org.simple.clinic.patient.ReminderConsent.Granted
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.toOptional
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ProgressMaterialButton
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.scrollToChild
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import org.simple.clinic.widgets.textChanges
import org.simple.clinic.widgets.topRelativeTo
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class PatientEntryScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), PatientEntryUi, PatientEntryValidationActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var phoneNumberValidator: PhoneNumberValidator

  @Inject
  lateinit var dobValidator: UserInputDateValidator

  @Inject
  lateinit var ageValidator: UserInputAgeValidator

  @Inject
  lateinit var country: Country

  @Inject
  lateinit var effectHandlerInjectionFactory: PatientEntryEffectHandler.InjectionFactory

  @Inject
  lateinit var features: Features

  private var binding: ScreenManualPatientEntryBinding? = null

  private val fullNameEditText
    get() = binding!!.fullNameEditText

  private val ageEditText
    get() = binding!!.ageEditText

  private val dateOfBirthEditText
    get() = binding!!.dateOfBirthEditText

  private val phoneNumberEditText
    get() = binding!!.phoneNumberEditText

  private val colonyOrVillageEditText
    get() = binding!!.colonyOrVillageEditText

  private val districtEditText
    get() = binding!!.districtEditText

  private val stateEditText
    get() = binding!!.stateEditText

  private val backButton
    get() = binding!!.backButton

  private val identifierTextView
    get() = binding!!.identifierTextView

  private val fullNameInputLayout
    get() = binding!!.fullNameInputLayout

  private val ageEditTextInputLayout
    get() = binding!!.ageEditTextInputLayout

  private val dateOfBirthInputLayout
    get() = binding!!.dateOfBirthInputLayout

  private val phoneNumberInputLayout
    get() = binding!!.phoneNumberInputLayout

  private val genderRadioGroup
    get() = binding!!.genderRadioGroup

  private val alternativeIdInputLayout
    get() = binding!!.alternativeIdInputLayout

  private val streetAddressInputLayout
    get() = binding!!.streetAddressInputLayout

  private val colonyOrVillageInputLayout
    get() = binding!!.colonyOrVillageInputLayout

  private val zoneInputLayout
    get() = binding!!.zoneInputLayout

  private val districtInputLayout
    get() = binding!!.districtInputLayout

  private val stateInputLayout
    get() = binding!!.stateInputLayout

  private val maleRadioButton
    get() = binding!!.maleRadioButton

  private val femaleRadioButton
    get() = binding!!.femaleRadioButton

  private val transgenderRadioButton
    get() = binding!!.transgenderRadioButton

  private val consentTextView
    get() = binding!!.consentTextView

  private val consentLabel
    get() = binding!!.consentLabel

  private val alternativeIdInputEditText
    get() = binding!!.alternativeIdInputEditText

  private val zoneEditText
    get() = binding!!.zoneEditText

  private val streetAddressEditText
    get() = binding!!.streetAddressEditText

  private val consentSwitch
    get() = binding!!.consentSwitch

  private val dateOfBirthAndAgeSeparator
    get() = binding!!.dateOfBirthAndAgeSeparator

  private val genderErrorTextView
    get() = binding!!.genderErrorTextView

  private val formScrollView
    get() = binding!!.formScrollView

  private val patientEntryRoot
    get() = binding!!.patientEntryRoot

  private val identifierContainer
    get() = binding!!.identifierContainer

  private val saveButton
    get() = binding!!.saveButton

  private val identifierTypeTextView
    get() = binding!!.identifierTypeTextView

  private val villageTypeAheadAdapter by unsafeLazy {
    ArrayAdapter<String>(
        context,
        R.layout.village_typeahead_list_item,
        R.id.villageTypeAheadItemTextView,
        mutableListOf()
    )
  }

  // FIXME This is temporally coupled to `scrollToFirstFieldWithError()`.
  private val allTextInputFields: List<EditText> by unsafeLazy {
    val ageOrDateOfBirthEditText = if (ageEditTextInputLayout.visibility == View.VISIBLE) {
      ageEditText
    } else {
      dateOfBirthEditText
    }
    listOf(
        fullNameEditText,
        ageOrDateOfBirthEditText,
        phoneNumberEditText,
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

  private val uiRenderer = PatientEntryUiRenderer(this)

  private val events: Observable<PatientEntryEvent> by unsafeLazy {
    Observable
        .merge(formChanges(), saveClicks(), consentChanges())
        .compose(ReportAnalyticsEvents())
        .cast<PatientEntryEvent>()
  }

  private val delegate by unsafeLazy {
    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = PatientEntryModel.DEFAULT,
        init = PatientEntryInit(isVillageTypeAheadEnabled = features.isEnabled(Feature.VillageTypeAhead)),
        update = PatientEntryUpdate(phoneNumberValidator, dobValidator, ageValidator),
        effectHandler = effectHandlerInjectionFactory.create(ui = this, validationActions = this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenManualPatientEntryBinding.bind(this)

    context.injector<Injector>().inject(this)

    backButton.setOnClickListener { router.pop() }

    if (features.isEnabled(Feature.VillageTypeAhead)) {
      colonyOrVillageEditText.setAdapter(villageTypeAheadAdapter)
    }
  }

  override fun setupUi(inputFields: InputFields) {
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

    setConsentText()
    setConsentLabelText()

    showOrHideInputFields(inputFields)
    setInputFieldsHint(inputFields)
    showOrHideGenderRadioButtons(inputFields)
  }

  override fun setColonyOrVillagesAutoComplete(colonyOrVillageList: List<String>) {
    villageTypeAheadAdapter.clear()
    villageTypeAheadAdapter.addAll(colonyOrVillageList)
  }

  private fun showOrHideInputFields(inputFields: InputFields) {
    val allTypesOfInputFields: Map<Class<*>, View> = mapOf(
        PatientNameField::class.java to fullNameInputLayout,
        LandlineOrMobileField::class.java to phoneNumberInputLayout,
        GenderField::class.java to genderRadioGroup,
        AlternativeIdInputField::class.java to alternativeIdInputLayout,
        StreetAddressField::class.java to streetAddressInputLayout,
        VillageOrColonyField::class.java to colonyOrVillageInputLayout,
        ZoneField::class.java to zoneInputLayout,
        DistrictField::class.java to districtInputLayout,
        StateField::class.java to stateInputLayout
    )

    val receivedTypesOfInputFields = inputFields.fields.map { it::class.java }

    allTypesOfInputFields.forEach { (clazz, view) ->
      view.visibleOrGone(clazz in receivedTypesOfInputFields)
    }
  }

  private fun setInputFieldsHint(inputFields: InputFields) {
    val allTextInputFields: Map<Class<*>, TextInputLayout> = mapOf(
        PatientNameField::class.java to fullNameInputLayout,
        LandlineOrMobileField::class.java to phoneNumberInputLayout,
        AlternativeIdInputField::class.java to alternativeIdInputLayout,
        StreetAddressField::class.java to streetAddressInputLayout,
        VillageOrColonyField::class.java to colonyOrVillageInputLayout,
        ZoneField::class.java to zoneInputLayout,
        DistrictField::class.java to districtInputLayout,
        StateField::class.java to stateInputLayout
    )

    inputFields.fields.forEach {
      allTextInputFields[it::class.java]?.hint = context.getString(it.labelResId)
    }
  }

  private fun showOrHideGenderRadioButtons(inputFields: InputFields) {
    val allGendersRadioButtons = mapOf(
        Male to maleRadioButton,
        Female to femaleRadioButton,
        Transgender to transgenderRadioButton
    )
    val genderField = inputFields.fields.find { it is GenderField } as GenderField

    allGendersRadioButtons.forEach { (gender, radioButton) ->
      radioButton.visibleOrGone(gender in genderField.allowedGenders)
    }
  }

  private fun setConsentText() {
    val consentText = buildSpannedString {
      inSpans(StyleSpan(BOLD)) {
        append(resources.getString(R.string.patiententry_consent_header))
      }

      append(resources.getString(R.string.patiententry_consent_first_para))
      append("\n\n")
      append(resources.getString(R.string.patiententry_consent_second_para))
    }
    consentTextView.text = consentText
  }

  private fun setConsentLabelText() {
    val consentLabelTextResId = if (country.areWhatsAppRemindersSupported)
      R.string.patiententry_consent_whatsapp_sms_reminders
    else
      R.string.patiententry_consent_sms_reminders
    consentLabel.setText(consentLabelTextResId)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    val viewState = delegate.onRestoreInstanceState(state)
    super.onRestoreInstanceState(viewState)
  }

  private fun formChanges(): Observable<PatientEntryEvent> {
    val alternativeIdInputEditTextChanges = Maybe.fromCallable {
      country.alternativeIdentifierType
    }.flatMapObservable { identifierType ->
      alternativeIdInputEditText.textChanges { AlternativeIdChanged(Identifier(it, identifierType)) }
    }

    return Observable.mergeArray(
        fullNameEditText.textChanges(::FullNameChanged),
        phoneNumberEditText.textChanges(::PhoneNumberChanged),
        dateOfBirthEditText.textChanges(::DateOfBirthChanged),
        dateOfBirthEditText.focusChanges.map(::DateOfBirthFocusChanged),
        ageEditText.textChanges(::AgeChanged),
        colonyOrVillageEditText.textChanges(::ColonyOrVillageChanged),
        districtEditText.textChanges(::DistrictChanged),
        stateEditText.textChanges(::StateChanged),
        genderChanges(),
        alternativeIdInputEditTextChanges,
        zoneEditText.textChanges(::ZoneChanged),
        streetAddressEditText.textChanges(::StreetAddressChanged)
    )
  }

  private fun genderChanges(): Observable<GenderChanged> {
    val radioIdToGenders = mapOf(
        R.id.femaleRadioButton to Female,
        R.id.maleRadioButton to Male,
        R.id.transgenderRadioButton to Transgender)

    return genderRadioGroup
        .checkedChanges()
        .map { checkedId ->
          val gender = radioIdToGenders[checkedId]
          GenderChanged(gender.toOptional())
        }
  }

  private fun saveClicks(): Observable<PatientEntryEvent> {
    val stateImeClicks = stateEditText
        .editorActions() { it == EditorInfo.IME_ACTION_DONE }
        .map { SaveClicked }

    val saveButtonClicks = saveButton
        .clicks()
        .map { SaveClicked }

    return saveButtonClicks
        .mergeWith(stateImeClicks)
        .cast()
  }

  private fun consentChanges(): Observable<PatientEntryEvent> =
      consentSwitch
          .checkedChanges()
          .map { checked -> if (checked) Granted else Denied }
          .map(::ReminderConsentChanged)

  override fun prefillFields(entry: OngoingNewPatientEntry) {
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
    when (identifier.type) {
      Identifier.IdentifierType.IndiaNationalHealthId -> prefillIndiaNationalHealthID(identifier)
      Identifier.IdentifierType.BpPassport -> prefillBpPassport(identifier)
      else -> throw IllegalArgumentException("Unknown alternate id: $identifier")
    }
  }

  private fun prefillIndiaNationalHealthID(identifier: Identifier) {
    identifierTypeTextView.text = resources.getString(R.string.patiententry_identifier_national_health_id)
    identifierTextView.text = identifier.displayValue()
  }

  private fun prefillBpPassport(identifier: Identifier) {
    identifierTypeTextView.text = resources.getString(R.string.patiententry_identifier_bp_passport)
    identifierTextView.text = identifier.displayValue()
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
    hideKeyboard()
    router.push(NewMedicalHistoryScreenKey().wrap())
  }

  override fun setDateOfBirthAndAgeVisibility(visibility: DateOfBirthAndAgeVisibility) {
    val transition = TransitionSet()
        .addTransition(ChangeBounds())
        .addTransition(Fade())
        .setOrdering(TransitionSet.ORDERING_TOGETHER)
        .setDuration(250)
        .setInterpolator(FastOutSlowInInterpolator())
    TransitionManager.beginDelayedTransition(this, transition)

    dateOfBirthInputLayout.visibility = when (visibility) {
      DATE_OF_BIRTH_VISIBLE, BOTH_VISIBLE -> View.VISIBLE
      else -> View.GONE
    }

    dateOfBirthAndAgeSeparator.visibility = when (visibility) {
      BOTH_VISIBLE -> View.VISIBLE
      else -> View.GONE
    }

    ageEditTextInputLayout.visibility = when (visibility) {
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

  override fun showLengthTooShortPhoneNumberError(show: Boolean, requiredNumberLength: Int) {
    if (show) {
      phoneNumberInputLayout.error = context.getString(R.string.patiententry_error_phonenumber_length_less, requiredNumberLength.toString())
    } else {
      phoneNumberInputLayout.error = null
    }
  }

  override fun showLengthTooLongPhoneNumberError(show: Boolean, requiredNumberLength: Int) {
    if (show) {
      phoneNumberInputLayout.error = context.getString(R.string.patiententry_error_phonenumber_length_more, requiredNumberLength.toString())
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

  override fun showAgeExceedsMaxLimitError(show: Boolean) {
    ageEditTextInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_age_exceeds_max_limit_error)
      else -> null
    }
  }

  override fun showDOBExceedsMaxLimitError(show: Boolean) {
    dateOfBirthInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_age_exceeds_max_limit_error)
      else -> null
    }
  }

  override fun showAgeExceedsMinLimitError(show: Boolean) {
    ageEditTextInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_age_exceeds_min_limit_error)
      else -> null
    }
  }

  override fun showDOBExceedsMinLimitError(show: Boolean) {
    dateOfBirthInputLayout.error = when {
      show -> resources.getString(R.string.patiententry_age_exceeds_min_limit_error)
      else -> null
    }
  }


  override fun scrollToFirstFieldWithError() {
    // FIXME This is temporally coupled to the layout and changes whenever the order of the fields change.
    //       This should be automatically retrieved from the inflated XML.
    //       Also, we need to add all the input fields and scroll to the ones that are 'visible' and not the
    //       ones that are 'gone'.
    val views = arrayOf(
        fullNameInputLayout,
        ageEditTextInputLayout,
        dateOfBirthInputLayout,
        genderErrorTextView,
        phoneNumberInputLayout,
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

  override fun scrollFormOnGenderSelection() {
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

  override fun nextButtonShowInProgress() {
    saveButton.setButtonState(ProgressMaterialButton.ButtonState.InProgress)
  }

  override fun enableNextButton() {
    saveButton.setButtonState(ProgressMaterialButton.ButtonState.Enabled)
  }

  interface Injector {
    fun inject(target: PatientEntryScreen)
  }
}
