package org.simple.clinic.newentry

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.simple.clinic.appconfig.Country
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.newentry.Field.Age
import org.simple.clinic.newentry.Field.ColonyOrVillage
import org.simple.clinic.newentry.Field.DateOfBirth
import org.simple.clinic.newentry.Field.District
import org.simple.clinic.newentry.Field.FullName
import org.simple.clinic.newentry.Field.Gender
import org.simple.clinic.newentry.Field.PhoneNumber
import org.simple.clinic.newentry.Field.State
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.Address
import org.simple.clinic.patient.PatientEntryValidationError
import org.simple.clinic.patient.PatientEntryValidationError.AgeExceedsMaxLimit
import org.simple.clinic.patient.PatientEntryValidationError.AgeExceedsMinLimit
import org.simple.clinic.patient.PatientEntryValidationError.BothDateOfBirthAndAgeAbsent
import org.simple.clinic.patient.PatientEntryValidationError.BothDateOfBirthAndAgePresent
import org.simple.clinic.patient.PatientEntryValidationError.ColonyOrVillageEmpty
import org.simple.clinic.patient.PatientEntryValidationError.DateOfBirthInFuture
import org.simple.clinic.patient.PatientEntryValidationError.DistrictEmpty
import org.simple.clinic.patient.PatientEntryValidationError.DobExceedsMaxLimit
import org.simple.clinic.patient.PatientEntryValidationError.DobExceedsMinLimit
import org.simple.clinic.patient.PatientEntryValidationError.EmptyAddressDetails
import org.simple.clinic.patient.PatientEntryValidationError.FullNameEmpty
import org.simple.clinic.patient.PatientEntryValidationError.InvalidDateOfBirth
import org.simple.clinic.patient.PatientEntryValidationError.MissingGender
import org.simple.clinic.patient.PatientEntryValidationError.PersonalDetailsEmpty
import org.simple.clinic.patient.PatientEntryValidationError.PhoneNumberLengthTooLong
import org.simple.clinic.patient.PatientEntryValidationError.PhoneNumberLengthTooShort
import org.simple.clinic.patient.PatientEntryValidationError.PhoneNumberNonNullButBlank
import org.simple.clinic.patient.PatientEntryValidationError.StateEmpty
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.ValueChangedCallback
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Named

class PatientEntryEffectHandler @AssistedInject constructor(
    private val facilityRepository: FacilityRepository,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    private val inputFieldsFactory: InputFieldsFactory,
    @Named("number_of_patients_registered") private val patientRegisteredCount: Preference<Int>,
    @Assisted private val ui: PatientEntryUi,
    @Assisted private val validationActions: PatientEntryValidationActions
) {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(
        ui: PatientEntryUi,
        validationActions: PatientEntryValidationActions
    ): PatientEntryEffectHandler
  }

  fun build(): ObservableTransformer<PatientEntryEffect, PatientEntryEvent> {
    val showDatePatternInLabelValueChangedCallback = ValueChangedCallback<Boolean>()

    return RxMobius
        .subtypeEffectHandler<PatientEntryEffect, PatientEntryEvent>()
        .addTransformer(FetchPatientEntry::class.java, fetchOngoingEntryTransformer(schedulersProvider.io()))
        .addConsumer(PrefillFields::class.java, { ui.prefillFields(it.patientEntry) }, schedulersProvider.ui())
        .addAction(ScrollFormOnGenderSelection::class.java, ui::scrollFormOnGenderSelection, schedulersProvider.ui())
        .addConsumer(HideValidationError::class.java, { hideValidationError(it.field) }, schedulersProvider.ui())
        .addConsumer(ShowDatePatternInDateOfBirthLabel::class.java, {
          showDatePatternInLabelValueChangedCallback.pass(it.show, ui::setShowDatePatternInDateOfBirthLabel)
        }, schedulersProvider.ui())
        .addTransformer(SavePatient::class.java, savePatientTransformer(schedulersProvider.io()))
        .addConsumer(ShowValidationErrors::class.java, { showValidationErrors(it.errors) }, schedulersProvider.ui())
        .addAction(OpenMedicalHistoryEntryScreen::class.java, ui::openMedicalHistoryEntryScreen, schedulersProvider.ui())
        .addTransformer(LoadInputFields::class.java, loadInputFields())
        .addConsumer(SetupUi::class.java, { ui.setupUi(it.inputFields) }, schedulersProvider.ui())
        .build()
  }

  private fun fetchOngoingEntryTransformer(scheduler: Scheduler): ObservableTransformer<FetchPatientEntry, PatientEntryEvent> {
    return ObservableTransformer { fetchPatientEntries ->
      val getPatientEntryAndFacility = Singles
          .zip(
              patientRepository.ongoingEntry(),
              facilityRepository.currentFacility().firstOrError()
          )

      fetchPatientEntries
          .flatMapSingle { getPatientEntryAndFacility }
          .subscribeOn(scheduler)
          .map { (entry, facility) ->
            entry.takeIf { it.address != null } ?: entry.withAddress(Address.withDistrictAndState(facility.district, facility.state))
          }
          .map { OngoingEntryFetched(it) }
    }
  }

  private fun hideValidationError(field: Field) {
    when (field) {
      FullName -> validationActions.showEmptyFullNameError(false)
      PhoneNumber -> hidePhoneLengthErrors()
      Age, DateOfBirth -> hideDateOfBirthErrors()
      Gender -> validationActions.showMissingGenderError(false)
      ColonyOrVillage -> validationActions.showEmptyColonyOrVillageError(false)
      District -> validationActions.showEmptyDistrictError(false)
      State -> validationActions.showEmptyStateError(false)
    }
  }

  private fun hidePhoneLengthErrors() {
    with(validationActions) {
      showLengthTooLongPhoneNumberError(false, 0)
      showLengthTooShortPhoneNumberError(false, 0)
    }
  }

  private fun hideDateOfBirthErrors() {
    with(validationActions) {
      showEmptyDateOfBirthAndAgeError(false)
      showInvalidDateOfBirthError(false)
      showDateOfBirthIsInFutureError(false)
      showAgeExceedsMaxLimitError(false)
      showDOBExceedsMaxLimitError(false)
      showAgeExceedsMinLimitError(false)
      showDOBExceedsMinLimitError(false)
    }
  }

  private fun savePatientTransformer(scheduler: Scheduler): ObservableTransformer<SavePatient, PatientEntryEvent> {
    return ObservableTransformer { savePatientEffects ->
      savePatientEffects
          .map { it.entry }
          .subscribeOn(scheduler)
          .flatMapSingle { savePatientEntry(it) }
          .doOnNext { patientRegisteredCount.set(patientRegisteredCount.get().plus(1)) }
          .map { PatientEntrySaved }
    }
  }

  private fun savePatientEntry(entry: OngoingNewPatientEntry): Single<Unit> {
    return patientRepository
        .saveOngoingEntry(entry)
        .andThen(Single.just(Unit))
  }

  private fun showValidationErrors(errors: List<PatientEntryValidationError>) {
    errors
        .onEach { Analytics.reportInputValidationError(it.analyticsName) }
        .forEach {
          when (it) {
            FullNameEmpty -> validationActions.showEmptyFullNameError(true)
            is PhoneNumberLengthTooShort -> validationActions.showLengthTooShortPhoneNumberError(true, it.limit)
            is PhoneNumberLengthTooLong -> validationActions.showLengthTooLongPhoneNumberError(true, it.limit)
            BothDateOfBirthAndAgeAbsent -> validationActions.showEmptyDateOfBirthAndAgeError(true)
            InvalidDateOfBirth -> validationActions.showInvalidDateOfBirthError(true)
            DateOfBirthInFuture -> validationActions.showDateOfBirthIsInFutureError(true)
            MissingGender -> validationActions.showMissingGenderError(true)
            ColonyOrVillageEmpty -> validationActions.showEmptyColonyOrVillageError(true)
            DistrictEmpty -> validationActions.showEmptyDistrictError(true)
            StateEmpty -> validationActions.showEmptyStateError(true)
            AgeExceedsMaxLimit -> validationActions.showAgeExceedsMaxLimitError(true)
            DobExceedsMaxLimit -> validationActions.showDOBExceedsMaxLimitError(true)
            AgeExceedsMinLimit -> validationActions.showAgeExceedsMinLimitError(true)
            DobExceedsMinLimit -> validationActions.showDOBExceedsMinLimitError(true)

            EmptyAddressDetails,
            PhoneNumberNonNullButBlank,
            BothDateOfBirthAndAgePresent,
            PersonalDetailsEmpty -> {
              throw AssertionError("Should never receive this error: $it")
            }
          }
        }

    if (errors.isNotEmpty()) {
      ui.scrollToFirstFieldWithError()
    }
  }

  private fun loadInputFields(): ObservableTransformer<LoadInputFields, PatientEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { inputFieldsFactory.provideFields() }
          .map(::InputFields)
          .map(::InputFieldsLoaded)
    }
  }
}
