package org.simple.clinic.editpatient

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.PatientEditBpPassportViewBinding
import org.simple.clinic.databinding.ScreenEditPatientBinding
import org.simple.clinic.di.injector
import org.simple.clinic.editpatient.EditPatientValidationError.AgeExceedsMaxLimit
import org.simple.clinic.editpatient.EditPatientValidationError.AgeExceedsMinLimit
import org.simple.clinic.editpatient.EditPatientValidationError.BothDateOfBirthAndAgeAdsent
import org.simple.clinic.editpatient.EditPatientValidationError.ColonyOrVillageEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthExceedsMaxLimit
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthExceedsMinLimit
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthInFuture
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthParseError
import org.simple.clinic.editpatient.EditPatientValidationError.DistrictEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.FullNameEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberLengthTooLong
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberLengthTooShort
import org.simple.clinic.editpatient.EditPatientValidationError.StateEmpty
import org.simple.clinic.editpatient.deletepatient.DeletePatientScreenKey
import org.simple.clinic.feature.Feature.DeletePatient
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.form.AgeField
import org.simple.clinic.newentry.form.AlternativeIdInputField
import org.simple.clinic.newentry.form.DateOfBirthField
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
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.scrollToChild
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.textChanges
import org.simple.clinic.widgets.visibleOrGone
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class EditPatientScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet), EditPatientUi, HandlesBack {

  @Inject
  lateinit var router: Router

  @Inject
  @Named("date_for_user_input")
  lateinit var dateOfBirthFormat: DateTimeFormatter

  @Inject
  lateinit var numberValidator: PhoneNumberValidator

  @Inject
  lateinit var dateOfBirthValidator: UserInputDateValidator

  @Inject
  lateinit var ageValidator: UserInputAgeValidator

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var effectHandlerFactory: EditPatientEffectHandler.Factory

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private val screenKey by unsafeLazy {
    screenKeyProvider.keyFor<EditPatientScreenKey>(this)
  }

  private var binding: ScreenEditPatientBinding? = null

  private val zoneEditText
    get() = binding!!.zoneEditText

  private val streetAddressEditText
    get() = binding!!.streetAddressEditText

  private val deletePatient
    get() = binding!!.deletePatient

  private val fullNameInputLayout
    get() = binding!!.fullNameInputLayout

  private val ageInputLayout
    get() = binding!!.ageInputLayout

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

  private val fullNameEditText
    get() = binding!!.fullNameEditText

  private val phoneNumberEditText
    get() = binding!!.phoneNumberEditText

  private val districtEditText
    get() = binding!!.districtEditText

  private val stateEditText
    get() = binding!!.stateEditText

  private val alternativeIdInputEditText
    get() = binding!!.alternativeIdInputEditText

  private val colonyOrVillageEditText
    get() = binding!!.colonyOrVillageEditText

  private val backButton
    get() = binding!!.backButton

  private val dateOfBirthEditText
    get() = binding!!.dateOfBirthEditText

  private val ageEditText
    get() = binding!!.ageEditText

  private val bpPassportsContainer
    get() = binding!!.bpPassportsContainer

  private val bpPassportsLabel
    get() = binding!!.bpPassportsLabel

  private val formScrollView
    get() = binding!!.formScrollView

  private val dateOfBirthAndAgeSeparator
    get() = binding!!.dateOfBirthAndAgeSeparator

  private val saveButton
    get() = binding!!.saveButton

  private val viewRenderer = EditPatientViewRenderer(this)

  private val hardwareBackPressEvents = PublishSubject.create<BackClicked>()

  private val events: Observable<EditPatientEvent>
    get() = Observable.mergeArray(
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
        backClicks(),
        bangladeshNationalIdChanges(),
        zoneEditText.textChanges(::ZoneChanged),
        streetAddressEditText.textChanges(::StreetAddressChanged)
    ).compose(ReportAnalyticsEvents())
        .cast()

  private val delegate by unsafeLazy {
    val (patient, address, phoneNumber, bangladeshNationalId) = screenKey

    MobiusDelegate.forView(
        events = events,
        defaultModel = EditPatientModel.from(patient, address, phoneNumber, dateOfBirthFormat, bangladeshNationalId, EditPatientState.NOT_SAVING_PATIENT),
        init = EditPatientInit(patient, address, phoneNumber, bangladeshNationalId),
        update = EditPatientUpdate(numberValidator, dateOfBirthValidator, ageValidator),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = viewRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenEditPatientBinding.bind(this)

    context.injector<Injector>().inject(this)
  }

  override fun setupUi(inputFields: InputFields) {
    showOrHideInputFields(inputFields)
    setInputFieldsHint(inputFields)
    showOrHideGenderRadioButtons(inputFields)

    deletePatient.visibleOrGone(features.isEnabled(DeletePatient))
    deletePatient.setOnClickListener { router.push(DeletePatientScreenKey(screenKey.patient.uuid).wrap()) }
  }

  override fun showColonyOrVillagesList(colonyOrVillageList: List<String>) {

  }

  private fun showOrHideInputFields(inputFields: InputFields) {
    val allTypesOfInputFields: Map<Class<*>, View> = mapOf(
        PatientNameField::class.java to fullNameInputLayout,
        AgeField::class.java to ageInputLayout,
        DateOfBirthField::class.java to dateOfBirthInputLayout,
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

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    val viewState = delegate.onRestoreInstanceState(state)
    super.onRestoreInstanceState(viewState)
  }

  private fun saveClicks(): Observable<EditPatientEvent> {
    return saveButton.clicks().map { SaveClicked }
  }

  private fun nameTextChanges(): Observable<EditPatientEvent> {
    return fullNameEditText.textChanges().map { NameChanged(it.toString()) }
  }

  private fun phoneNumberTextChanges(): Observable<EditPatientEvent> {
    return phoneNumberEditText.textChanges().map { PhoneNumberChanged(it.toString()) }
  }

  private fun districtTextChanges(): Observable<EditPatientEvent> {
    return districtEditText.textChanges().map { DistrictChanged(it.toString()) }
  }

  private fun stateTextChanges(): Observable<EditPatientEvent> {
    return stateEditText.textChanges().map { StateChanged(it.toString()) }
  }

  private fun bangladeshNationalIdChanges(): Observable<EditPatientEvent> {
    return alternativeIdInputEditText.textChanges().map { AlternativeIdChanged(it.toString()) }
  }

  private fun colonyTextChanges(): Observable<EditPatientEvent> {
    return colonyOrVillageEditText.textChanges().map { ColonyOrVillageChanged(it.toString()) }
  }

  private fun backClicks(): Observable<EditPatientEvent> {
    val backButtonClicks = backButton
        .clicks()
        .map { BackClicked }

    return backButtonClicks
        .mergeWith(hardwareBackPressEvents)
        .cast()
  }

  private fun genderChanges(): Observable<EditPatientEvent> {
    val radioIdToGenders = mapOf(
        R.id.femaleRadioButton to Female,
        R.id.maleRadioButton to Male,
        R.id.transgenderRadioButton to Transgender)

    return genderRadioGroup
        .checkedChanges()
        .filter { it != -1 }
        .map { checkedId ->
          val gender = radioIdToGenders.getValue(checkedId)
          GenderChanged(gender)
        }
  }

  private fun dateOfBirthTextChanges(): Observable<EditPatientEvent> = dateOfBirthEditText.textChanges(::DateOfBirthChanged)

  private fun dateOfBirthFocusChanges(): Observable<EditPatientEvent> = dateOfBirthEditText.focusChanges.map(::DateOfBirthFocusChanged)

  private fun ageTextChanges(): Observable<EditPatientEvent> = ageEditText.textChanges(::AgeChanged)

  override fun displayBpPassports(identifiers: List<String>) {
    bpPassportsContainer.removeAllViews()
    identifiers.forEach { identifier -> inflateBpPassportView(identifier) }

    bpPassportsLabel.visibleOrGone(identifiers.isNotEmpty())

    post { requestLayout() }
  }

  private fun inflateBpPassportView(identifier: String) {
    val layoutInflater = LayoutInflater.from(context)
    val bpPassportView = PatientEditBpPassportViewBinding.inflate(layoutInflater, this, false)
    bpPassportView.bpPassportIdentifier.text = identifier
    bpPassportsContainer.addView(bpPassportView.root)
  }

  override fun setPatientName(name: String) {
    fullNameEditText.setTextAndCursor(name)
  }

  override fun setPatientPhoneNumber(number: String) {
    phoneNumberEditText.setTextAndCursor(number)
  }

  override fun setColonyOrVillage(colonyOrVillage: String) {
    colonyOrVillageEditText.setTextAndCursor(colonyOrVillage)
  }

  override fun setDistrict(district: String) {
    districtEditText.setTextAndCursor(district)
  }

  override fun setState(state: String) {
    stateEditText.setTextAndCursor(state)
  }

  override fun setStreetAddress(streetAddress: String?) {
    streetAddressEditText.setTextAndCursor(streetAddress)
  }

  override fun setZone(zone: String?) {
    zoneEditText.setTextAndCursor(zone)
  }

  override fun setGender(gender: Gender) {
    val genderButton: RadioButton? = when (gender) {
      Male -> maleRadioButton
      Female -> femaleRadioButton
      Transgender -> transgenderRadioButton
      is Unknown -> {
        crashReporter.report(IllegalStateException("Heads-up: unknown gender ${gender.actualValue} found in ${EditPatientScreen::class.java.name}"))
        null
      }
    }

    genderButton?.isChecked = true
  }

  override fun setPatientAge(age: Int) {
    ageEditText.setTextAndCursor(age.toString())
  }

  override fun setPatientDateOfBirth(dateOfBirth: LocalDate) {
    dateOfBirthEditText.setTextAndCursor(dateOfBirthFormat.format(dateOfBirth))
  }

  override fun showValidationErrors(errors: Set<EditPatientValidationError>) {
    errors.forEach {
      when (it) {
        FullNameEmpty -> showEmptyFullNameError(true)
        PhoneNumberEmpty -> showPhoneNumberEmptyError()
        is PhoneNumberLengthTooShort -> showLengthTooShortPhoneNumberError(it.minimumAllowedNumberLength)
        is PhoneNumberLengthTooLong -> showLengthTooLongPhoneNumberError(it.maximumAllowedNumberLength)
        ColonyOrVillageEmpty -> showEmptyColonyOrVillageError(true)
        DistrictEmpty -> showEmptyDistrictError(true)
        StateEmpty -> showEmptyStateError(true)
        BothDateOfBirthAndAgeAdsent -> showAgeEmptyError(true)
        DateOfBirthParseError -> showInvalidaDateOfBithError()
        DateOfBirthInFuture -> showDateOfBirthIsInFutureError()
        AgeExceedsMaxLimit -> showAgeExceedsMaxLimitError()
        DateOfBirthExceedsMaxLimit -> showDateOfBirthExceedsMaxLimitError()
        AgeExceedsMinLimit -> showAgeExceedsMinLimitError()
        DateOfBirthExceedsMinLimit -> showDateOfBirthExceedsMinLimitError()
      }.exhaustive()
    }
  }

  override fun hideValidationErrors(errors: Set<EditPatientValidationError>) {
    errors.forEach {
      when (it) {
        FullNameEmpty -> {
          showEmptyFullNameError(false)
        }

        PhoneNumberEmpty,
        is PhoneNumberLengthTooShort,
        is PhoneNumberLengthTooLong -> {
          hidePhoneNumberError()
        }

        ColonyOrVillageEmpty -> {
          showEmptyColonyOrVillageError(false)
        }

        DistrictEmpty -> {
          showEmptyDistrictError(false)
        }

        StateEmpty -> {
          showEmptyStateError(false)
        }

        BothDateOfBirthAndAgeAdsent,
        AgeExceedsMaxLimit,
        AgeExceedsMinLimit -> {
          showAgeEmptyError(false)
        }

        DateOfBirthParseError,
        DateOfBirthInFuture,
        DateOfBirthExceedsMaxLimit,
        DateOfBirthExceedsMinLimit -> {
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

  private fun showPhoneNumberEmptyError() {
    phoneNumberInputLayout.error = context.getString(R.string.patientedit_error_phonenumber_blank)
  }

  private fun showLengthTooShortPhoneNumberError(requiredNumberLength: Int) {
    phoneNumberInputLayout.error = context.getString(R.string.patientedit_error_phonenumber_length_less, requiredNumberLength.toString())
  }

  private fun showLengthTooLongPhoneNumberError(requiredNumberLength: Int) {
    phoneNumberInputLayout.error = context.getString(R.string.patientedit_error_phonenumber_length_more, requiredNumberLength.toString())
  }

  private fun showInvalidaDateOfBithError() {
    dateOfBirthInputLayout.error = context.getString(R.string.patientedit_error_invalid_dateofbirth)
  }

  private fun showDateOfBirthIsInFutureError() {
    dateOfBirthInputLayout.error = context.getString(R.string.patientedit_error_dateofbirth_is_in_future)
  }

  private fun showAgeExceedsMaxLimitError() {
    ageInputLayout.error = resources.getString(R.string.patiententry_age_exceeds_max_limit_error)
  }

  private fun showDateOfBirthExceedsMaxLimitError() {
    dateOfBirthInputLayout.error = resources.getString(R.string.patiententry_age_exceeds_max_limit_error)
  }

  private fun showAgeExceedsMinLimitError() {
    ageInputLayout.error = resources.getString(R.string.patiententry_age_exceeds_min_limit_error)
  }

  private fun showDateOfBirthExceedsMinLimitError() {
    dateOfBirthInputLayout.error = resources.getString(R.string.patiententry_age_exceeds_min_limit_error)
  }

  private fun hideDateOfBirthError() {
    dateOfBirthInputLayout.error = null
  }

  private fun hidePhoneNumberError() {
    phoneNumberInputLayout.error = null
  }

  override fun scrollToFirstFieldWithError() {
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

  override fun goBack() {
    router.pop()
  }

  override fun showDatePatternInDateOfBirthLabel() {
    dateOfBirthInputLayout.hint = resources.getString(R.string.patientedit_date_of_birth_focused)
  }

  override fun hideDatePatternInDateOfBirthLabel() {
    dateOfBirthInputLayout.hint = resources.getString(R.string.patientedit_date_of_birth_unfocused)
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
      DATE_OF_BIRTH_VISIBLE, BOTH_VISIBLE -> VISIBLE
      else -> GONE
    }

    dateOfBirthAndAgeSeparator.visibility = when (visibility) {
      BOTH_VISIBLE -> VISIBLE
      else -> GONE
    }

    ageInputLayout.visibility = when (visibility) {
      AGE_VISIBLE, BOTH_VISIBLE -> VISIBLE
      else -> GONE
    }
  }

  override fun showDiscardChangesAlert() {
    ConfirmDiscardChangesDialog.show(activity.supportFragmentManager)
  }

  override fun showProgress() {
    saveButton.setButtonState(InProgress)
  }

  override fun hideProgress() {
    saveButton.setButtonState(Enabled)
  }

  override fun setBangladeshNationalId(nationalId: String) {
    alternativeIdInputEditText.setTextAndCursor(nationalId)
  }

  override fun onBackPressed(): Boolean {
    hardwareBackPressEvents.onNext(BackClicked)
    return true
  }

  interface Injector {
    fun inject(target: EditPatientScreen)
  }
}
