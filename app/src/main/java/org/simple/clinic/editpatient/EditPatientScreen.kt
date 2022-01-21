package org.simple.clinic.editpatient

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isGone
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.activity.permissions.RequestPermissions
import org.simple.clinic.activity.permissions.RuntimePermissions
import org.simple.clinic.appconfig.Country
import org.simple.clinic.databinding.PatientEditAlternateIdViewBinding
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
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberLengthTooShort
import org.simple.clinic.editpatient.EditPatientValidationError.StateEmpty
import org.simple.clinic.editpatient.deletepatient.DeletePatientScreen
import org.simple.clinic.feature.Feature.AddingHealthIDsFromEditPatient
import org.simple.clinic.feature.Feature.DeletePatient
import org.simple.clinic.feature.Feature.VillageTypeAhead
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseScreen
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
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.scanid.OpenedFrom
import org.simple.clinic.scanid.ScanSimpleIdScreen
import org.simple.clinic.scanid.ScanSimpleIdScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.afterTextChangedWatcher
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.setFragmentResultListener
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.checkWithListener
import org.simple.clinic.widgets.scrollToChild
import org.simple.clinic.widgets.setTextWithWatcher
import org.simple.clinic.widgets.visibleOrGone
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class EditPatientScreen : BaseScreen<
    EditPatientScreen.Key,
    ScreenEditPatientBinding,
    EditPatientModel,
    EditPatientEvent,
    EditPatientEffect,
    EditPatientViewEffect>(), EditPatientUi, HandlesBack {

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
  lateinit var effectHandlerFactory: EditPatientEffectHandler.Factory

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  lateinit var screenResults: ScreenResultBus

   @Inject
  lateinit var country: Country

  private val rootView
    get() = binding.root

  private val zoneEditText
    get() = binding.zoneEditText

  private val streetAddressEditText
    get() = binding.streetAddressEditText

  private val deletePatient
    get() = binding.deletePatient

  private val fullNameInputLayout
    get() = binding.fullNameInputLayout

  private val ageInputLayout
    get() = binding.ageInputLayout

  private val dateOfBirthInputLayout
    get() = binding.dateOfBirthInputLayout

  private val phoneNumberInputLayout
    get() = binding.phoneNumberInputLayout

  private val genderRadioGroup
    get() = binding.genderRadioGroup

  private val alternativeIdInputLayout
    get() = binding.alternativeIdInputLayout

  private val streetAddressInputLayout
    get() = binding.streetAddressInputLayout

  private val colonyOrVillageInputLayout
    get() = binding.colonyOrVillageInputLayout

  private val zoneInputLayout
    get() = binding.zoneInputLayout

  private val districtInputLayout
    get() = binding.districtInputLayout

  private val stateInputLayout
    get() = binding.stateInputLayout

  private val maleRadioButton
    get() = binding.maleRadioButton

  private val femaleRadioButton
    get() = binding.femaleRadioButton

  private val transgenderRadioButton
    get() = binding.transgenderRadioButton

  private val fullNameEditText
    get() = binding.fullNameEditText

  private val phoneNumberEditText
    get() = binding.phoneNumberEditText

  private val districtEditText
    get() = binding.districtEditText

  private val stateEditText
    get() = binding.stateEditText

  private val alternativeIdInputEditText
    get() = binding.alternativeIdInputEditText

  private val colonyOrVillageEditText
    get() = binding.colonyOrVillageEditText

  private val backButton
    get() = binding.backButton

  private val dateOfBirthEditText
    get() = binding.dateOfBirthEditText

  private val ageEditText
    get() = binding.ageEditText

  private val bpPassportsContainer
    get() = binding.bpPassportsContainer

  private val bpPassportsLabel
    get() = binding.bpPassportsLabel

  private val alternateIdLabel
    get() = binding.alternateIdLabel

  private val alternateIdContainer
    get() = binding.alternateIdContainer

  private val formScrollView
    get() = binding.formScrollView

  private val dateOfBirthAndAgeSeparator
    get() = binding.dateOfBirthAndAgeSeparator

  private val saveButton
    get() = binding.saveButton

  private val addBpPassportButton
    get() = binding.addBpPassportButton

  private val addNHIDButton
    get() = binding.addNHIDButton

  private val hardwareBackPressEvents = PublishSubject.create<BackClicked>()
  private val hotEvents = PublishSubject.create<UiEvent>()
  private val additionalEvents = DeferredEventSource<EditPatientEvent>()

  private val villageTypeAheadAdapter by unsafeLazy {
    ArrayAdapter<String>(
        requireContext(),
        R.layout.village_typeahead_list_item,
        R.id.villageTypeAheadItemTextView,
        mutableListOf()
    )
  }

  private val fullNameEditTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      hotEvents.onNext(NameChanged(text?.toString().orEmpty()))
    }
  }

  private val phoneNumberTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      hotEvents.onNext(PhoneNumberChanged(text?.toString().orEmpty()))
    }
  }

  private val districtTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      hotEvents.onNext(DistrictChanged(text?.toString().orEmpty()))
    }
  }

  private val stateTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      hotEvents.onNext(StateChanged(text?.toString().orEmpty()))
    }
  }

  private val colonyOrVillageTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      hotEvents.onNext(ColonyOrVillageChanged(text?.toString().orEmpty()))
    }
  }

  private val alternateIdTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      hotEvents.onNext(AlternativeIdChanged(text?.toString().orEmpty()))
    }
  }

  private val dateOfBirthTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      hotEvents.onNext(DateOfBirthChanged(text?.toString().orEmpty()))
    }
  }

  private val ageTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      hotEvents.onNext(AgeChanged(text?.toString().orEmpty()))
    }
  }

  private val zoneTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      hotEvents.onNext(ZoneChanged(text?.toString().orEmpty()))
    }
  }

  private val streetAddressTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      hotEvents.onNext(StreetAddressChanged(text?.toString().orEmpty()))
    }
  }

  override fun defaultModel() = EditPatientModel.from(
      patient = screenKey.patient,
      address = screenKey.address,
      phoneNumber = screenKey.phoneNumber,
      dateOfBirthFormatter = dateOfBirthFormat,
      bangladeshNationalId = screenKey.bangladeshNationalId,
      saveButtonState = EditPatientState.NOT_SAVING_PATIENT,
      isUserCountryIndia = country.isoCountryCode == Country.INDIA,
      isAddingHealthIDsFromEditPatientEnabled = features.isEnabled(AddingHealthIDsFromEditPatient)
  )

  override fun createInit() = EditPatientInit(
      patient = screenKey.patient,
      isVillageTypeAheadEnabled = features.isEnabled(VillageTypeAhead)
  )

  override fun createUpdate() = EditPatientUpdate(numberValidator, dateOfBirthValidator, ageValidator)

  override fun createEffectHandler(viewEffectsConsumer: Consumer<EditPatientViewEffect>) = effectHandlerFactory
      .create(viewEffectsConsumer = viewEffectsConsumer)
      .build()

  override fun uiRenderer() = EditPatientViewRenderer(this)

  override fun viewEffectHandler() = EditPatientViewEffectHandler(this)

  override fun events() = Observable.mergeArray(
      saveClicks(),
      dateOfBirthFocusChanges(),
      backClicks(),
      addBpPassportClicks(),
      addNHIDButtonClicks(),
      hotEvents
  )
      .compose(RequestPermissions(runtimePermissions, screenResults.streamResults().ofType()))
      .compose(ReportAnalyticsEvents())
      .cast<EditPatientEvent>()

  override fun additionalEventSources() = listOf(
      additionalEvents
  )

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenEditPatientBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setAdapterWhenVillageTypeAheadIsEnabled()

    deletePatient.setOnClickListener { router.push(DeletePatientScreen.Key(screenKey.patient.uuid)) }

    handleScanIdentifierResult()
  }

  private fun setAdapterWhenVillageTypeAheadIsEnabled() {
    if (features.isEnabled(VillageTypeAhead)) {
      colonyOrVillageEditText.setAdapter(villageTypeAheadAdapter)
    }
  }

  private fun handleScanIdentifierResult() {
    setFragmentResultListener(ScanBpPassport, ScanIndiaNationalHealthID) { requestKey, result ->
      if (result !is Succeeded) return@setFragmentResultListener

      when (requestKey) {
        ScanBpPassport -> {
          val scannedBPPassport = ScanSimpleIdScreen.readIdentifier(result)
          additionalEvents.notify(BpPassportAdded(listOf(scannedBPPassport)))
        }
        ScanIndiaNationalHealthID -> {
          val scannedIndiaNHID = ScanSimpleIdScreen.readIdentifier(result)
          additionalEvents.notify(AlternativeIdChanged(scannedIndiaNHID.value))
        }
      }
    }
  }

  override fun setupUi(inputFields: InputFields) {
    showOrHideInputFields(inputFields)
    setInputFieldsHint(inputFields)
    showOrHideGenderRadioButtons(inputFields)

    deletePatient.visibleOrGone(features.isEnabled(DeletePatient))
  }

  override fun setColonyOrVillagesAutoComplete(colonyOrVillageList: List<String>) {
    villageTypeAheadAdapter.clear()
    villageTypeAheadAdapter.addAll(colonyOrVillageList)
  }

  override fun openSimpleScanIdScreen(openedFrom: OpenedFrom) {
    val requestType = when (openedFrom) {
      is OpenedFrom.EditPatientScreen.ToAddBpPassport -> ScanBpPassport
      is OpenedFrom.EditPatientScreen.ToAddNHID -> ScanIndiaNationalHealthID
      else -> throw IllegalArgumentException("Unknown opened from: $openedFrom")
    }

    router.pushExpectingResult(requestType, ScanSimpleIdScreenKey(openedFrom))
  }

  override fun showAddNHIDButton() {
    addNHIDButton.visibility = VISIBLE
  }

  override fun hideAddNHIDButton() {
    addNHIDButton.visibility = GONE
  }

  override fun showIndiaNHIDLabel() {
    alternateIdLabel.visibility = VISIBLE
    alternateIdLabel.text = resources.getString(R.string.identifiertype_india_national_health_id)
  }

  override fun showBPPassportButton() {
    addBpPassportButton.visibility = VISIBLE
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
      setInputFieldsVisibility(
          clazz = clazz,
          view = view,
          isVisible = clazz in receivedTypesOfInputFields
      )
    }
  }

  private fun setInputFieldsVisibility(
      clazz: Class<*>,
      view: View,
      isVisible: Boolean
  ) {
    when {
      clazz == AgeField::class.java && view.isGone -> {
        return
      }
      clazz == DateOfBirthField::class.java && view.isGone -> {
        return
      }
      else -> view.visibleOrGone(isVisible)
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
      allTextInputFields[it::class.java]?.hint = getString(it.labelResId)
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

  private fun saveClicks(): Observable<EditPatientEvent> {
    return saveButton.clicks().map { SaveClicked }
  }

  private fun addNHIDButtonClicks(): Observable<EditPatientEvent> {
    return addNHIDButton.clicks().map { AddNHIDButtonClicked() }
  }

  private fun backClicks(): Observable<EditPatientEvent> {
    val backButtonClicks = backButton
        .clicks()
        .map { BackClicked }

    return backButtonClicks
        .mergeWith(hardwareBackPressEvents)
        .cast()
  }

  private fun addBpPassportClicks(): Observable<EditPatientEvent> {
    return addBpPassportButton.clicks().map { AddBpPassportButtonClicked() }
  }

  private fun dateOfBirthFocusChanges(): Observable<EditPatientEvent> = dateOfBirthEditText.focusChanges.map(::DateOfBirthFocusChanged)

  override fun displayBpPassports(identifiers: List<String>) {
    bpPassportsContainer.removeAllViews()
    identifiers.forEach { identifier -> inflateBpPassportView(identifier) }

    bpPassportsLabel.visibleOrGone(identifiers.isNotEmpty())
  }

  private fun inflateBpPassportView(identifier: String) {
    val layoutInflater = LayoutInflater.from(requireContext())
    val bpPassportView = PatientEditBpPassportViewBinding.inflate(layoutInflater, rootView, false)
    bpPassportView.bpPassportIdentifier.text = identifier
    bpPassportsContainer.addView(bpPassportView.root)
  }

  override fun setPatientName(name: String) {
    fullNameEditText.setTextWithWatcher(name, fullNameEditTextChanges)
  }

  override fun setPatientPhoneNumber(number: String) {
    phoneNumberEditText.setTextWithWatcher(number, phoneNumberTextChanges)
  }

  override fun setColonyOrVillage(colonyOrVillage: String) {
    colonyOrVillageEditText.setTextWithWatcher(colonyOrVillage, colonyOrVillageTextChanges)
  }

  override fun setDistrict(district: String) {
    districtEditText.setTextWithWatcher(district, districtTextChanges)
  }

  override fun setState(state: String) {
    stateEditText.setTextWithWatcher(state, stateTextChanges)
  }

  override fun setStreetAddress(streetAddress: String?) {
    streetAddressEditText.setTextWithWatcher(streetAddress, streetAddressTextChanges)
  }

  override fun setZone(zone: String?) {
    zoneEditText.setTextWithWatcher(zone, zoneTextChanges)
  }

  override fun setGender(gender: Gender) {
    val checkedId = when (gender) {
      Male -> maleRadioButton.id
      Female -> femaleRadioButton.id
      Transgender -> transgenderRadioButton.id
      is Unknown -> {
        CrashReporter.report(IllegalStateException("Heads-up: unknown gender ${gender.actualValue} found in ${EditPatientScreen::class.java.name}"))
        View.NO_ID
      }
    }

    val radioIdToGenders = mapOf(
        R.id.femaleRadioButton to Female,
        R.id.maleRadioButton to Male,
        R.id.transgenderRadioButton to Transgender)

    genderRadioGroup.checkWithListener(checkedId) { _, updatedCheckedId ->
      val updatedGender = radioIdToGenders.getValue(updatedCheckedId)
      hotEvents.onNext(GenderChanged(updatedGender))
    }
  }

  override fun setPatientAge(age: String) {
    ageEditText.setTextWithWatcher(age, ageTextChanges)
  }

  override fun setPatientDateOfBirth(dateOfBirth: String) {
    dateOfBirthEditText.setTextWithWatcher(dateOfBirth, dateOfBirthTextChanges)
  }

  override fun showValidationErrors(errors: Set<EditPatientValidationError>) {
    errors.forEach {
      when (it) {
        FullNameEmpty -> showEmptyFullNameError(true)
        PhoneNumberEmpty -> showPhoneNumberEmptyError()
        is PhoneNumberLengthTooShort -> showLengthTooShortPhoneNumberError(it.minimumAllowedNumberLength)
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
        is PhoneNumberLengthTooShort -> {
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
    phoneNumberInputLayout.error = getString(R.string.patientedit_error_phonenumber_blank)
  }

  private fun showLengthTooShortPhoneNumberError(requiredNumberLength: Int) {
    phoneNumberInputLayout.error = getString(R.string.patientedit_error_phonenumber_length_less, requiredNumberLength.toString())
  }

  private fun showInvalidaDateOfBithError() {
    dateOfBirthInputLayout.error = getString(R.string.patientedit_error_invalid_dateofbirth)
  }

  private fun showDateOfBirthIsInFutureError() {
    dateOfBirthInputLayout.error = getString(R.string.patientedit_error_dateofbirth_is_in_future)
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
    TransitionManager.beginDelayedTransition(rootView, transition)

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
    ConfirmDiscardChangesDialog.show(childFragmentManager)
  }

  override fun showProgress() {
    saveButton.setButtonState(InProgress)
  }

  override fun hideProgress() {
    saveButton.setButtonState(Enabled)
  }

  override fun setAlternateIdTextField(alternateId: String) {
    alternativeIdInputEditText.setTextWithWatcher(alternateId, alternateIdTextChanges)
  }

  override fun setAlternateIdContainer(alternateId: Identifier, hasHighlight: Boolean) {
    alternateIdLabel.visibility = VISIBLE
    alternateIdLabel.text = alternateId.displayType(resources)

    alternateIdContainer.visibility = VISIBLE

    inflateAlternateIdView(alternateId.displayValue(), hasHighlight)
  }

  private fun inflateAlternateIdView(identifier: String, hasHighlight: Boolean) {
    val layoutInflater = LayoutInflater.from(requireContext())
    val alternateIdView = PatientEditAlternateIdViewBinding.inflate(layoutInflater, rootView, false)
    alternateIdView.alternateIdentifier.text = identifier
    alternateIdContainer.removeAllViews()

    setHighlight(hasHighlight, alternateIdView)

    alternateIdContainer.addView(alternateIdView.root)
  }

  private fun setHighlight(
      hasHighlight: Boolean,
      alternateIdView: PatientEditAlternateIdViewBinding
  ) {
    if (hasHighlight) {
      alternateIdView.alternateIdentifier.setBackgroundColor(resources.getColor(R.color.simple_yellow_100))

      val horizontalPadding = resources.getDimensionPixelSize(R.dimen.spacing_8)
      val verticalPadding = resources.getDimensionPixelSize(R.dimen.spacing_4)

      alternateIdView.alternateIdentifier.setPaddingRelative(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }
  }

  override fun onBackPressed(): Boolean {
    hardwareBackPressEvents.onNext(BackClicked)
    return true
  }

  interface Injector {
    fun inject(target: EditPatientScreen)
  }

  @Parcelize
  data class Key(
      val patient: Patient,
      val address: PatientAddress,
      val phoneNumber: PatientPhoneNumber?,
      val bangladeshNationalId: BusinessId?,
      override val analyticsName: String = "Edit Patient"
  ) : ScreenKey() {

    override fun instantiateFragment() = EditPatientScreen()
  }

  @Parcelize
  object ScanBpPassport : Parcelable

  @Parcelize
  object ScanIndiaNationalHealthID : Parcelable
}
