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
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.android.synthetic.main.patient_edit_bp_passport_view.view.*
import kotlinx.android.synthetic.main.screen_edit_patient.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
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
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
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
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.ScreenRouter
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

class EditPatientScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet), EditPatientUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @field:[Inject Named("date_for_user_input")]
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
  lateinit var inputFields: InputFields

  @Inject
  lateinit var features: Features

  private val screenKey by unsafeLazy {
    screenRouter.key<EditPatientScreenKey>(this)
  }

  private val viewRenderer = EditPatientViewRenderer(this)

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

    MobiusDelegate(
        events,
        EditPatientModel.from(patient, address, phoneNumber, dateOfBirthFormat, bangladeshNationalId, EditPatientState.NOT_SAVING_PATIENT),
        EditPatientInit(patient, address, phoneNumber, bangladeshNationalId),
        EditPatientUpdate(numberValidator, dateOfBirthValidator, ageValidator),
        effectHandlerFactory.create(this).build(),
        viewRenderer::render,
        crashReporter
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    delegate.prepare()

    showOrHideInputFields()
    setInputFieldsHint()
    showOrHideGenderRadioButtons()

    deletePatient.visibleOrGone(features.isEnabled(DeletePatient))
    deletePatient.setOnClickListener { screenRouter.push(DeletePatientScreenKey(screenKey.patient.uuid)) }
  }

  private fun showOrHideInputFields() {
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

  private fun setInputFieldsHint() {
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

  private fun showOrHideGenderRadioButtons() {
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
    return RxView.clicks(saveButtonFrame.button).map { SaveClicked }
  }

  private fun nameTextChanges(): Observable<EditPatientEvent> {
    return RxTextView.textChanges(fullNameEditText).map { NameChanged(it.toString()) }
  }

  private fun phoneNumberTextChanges(): Observable<EditPatientEvent> {
    return RxTextView.textChanges(phoneNumberEditText).map { PhoneNumberChanged(it.toString()) }
  }

  private fun districtTextChanges(): Observable<EditPatientEvent> {
    return RxTextView.textChanges(districtEditText).map { DistrictChanged(it.toString()) }
  }

  private fun stateTextChanges(): Observable<EditPatientEvent> {
    return RxTextView.textChanges(stateEditText).map { StateChanged(it.toString()) }
  }

  private fun bangladeshNationalIdChanges(): Observable<EditPatientEvent> {
    return RxTextView.textChanges(alternativeIdInputEditText).map { AlternativeIdChanged(it.toString()) }
  }

  private fun colonyTextChanges(): Observable<EditPatientEvent> {
    return RxTextView.textChanges(colonyOrVillageEditText).map { ColonyOrVillageChanged(it.toString()) }
  }

  private fun backClicks(): Observable<EditPatientEvent> {
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
        .map { BackClicked }
  }

  private fun genderChanges(): Observable<EditPatientEvent> {
    val radioIdToGenders = mapOf(
        R.id.femaleRadioButton to Female,
        R.id.maleRadioButton to Male,
        R.id.transgenderRadioButton to Transgender)

    return RxRadioGroup.checkedChanges(genderRadioGroup)
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
    val bpPassportView = LayoutInflater.from(context).inflate(R.layout.patient_edit_bp_passport_view, bpPassportsContainer, false)
    bpPassportView.bpPassportIdentifier.text = identifier
    bpPassportsContainer.addView(bpPassportView)
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
    screenRouter.pop()
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
}
