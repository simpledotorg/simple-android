package org.simple.clinic.newentry

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.newentry.Field.Age
import org.simple.clinic.newentry.Field.ColonyOrVillage
import org.simple.clinic.newentry.Field.DateOfBirth
import org.simple.clinic.newentry.Field.District
import org.simple.clinic.newentry.Field.FullName
import org.simple.clinic.newentry.Field.Gender
import org.simple.clinic.newentry.Field.PhoneNumber
import org.simple.clinic.newentry.Field.State
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.Address
import org.simple.clinic.patient.PatientEntryValidationError
import org.simple.clinic.patient.PatientEntryValidationError.AGE_EXCEEDS_MAX_LIMIT
import org.simple.clinic.patient.PatientEntryValidationError.AGE_EXCEEDS_MIN_LIMIT
import org.simple.clinic.patient.PatientEntryValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.patient.PatientEntryValidationError.BOTH_DATEOFBIRTH_AND_AGE_PRESENT
import org.simple.clinic.patient.PatientEntryValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.patient.PatientEntryValidationError.DISTRICT_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.DOB_EXCEEDS_MAX_LIMIT
import org.simple.clinic.patient.PatientEntryValidationError.DOB_EXCEEDS_MIN_LIMIT
import org.simple.clinic.patient.PatientEntryValidationError.EMPTY_ADDRESS_DETAILS
import org.simple.clinic.patient.PatientEntryValidationError.FULL_NAME_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.patient.PatientEntryValidationError.MISSING_GENDER
import org.simple.clinic.patient.PatientEntryValidationError.PERSONAL_DETAILS_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.patient.PatientEntryValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.patient.PatientEntryValidationError.PHONE_NUMBER_NON_NULL_BUT_BLANK
import org.simple.clinic.patient.PatientEntryValidationError.STATE_EMPTY
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.ValueChangedCallback
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientEntryEffectHandler(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val patientRepository: PatientRepository,
    private val patientRegisteredCount: Preference<Int>,
    private val ui: PatientEntryUi,
    private val validationActions: PatientEntryValidationActions,
    private val schedulersProvider: SchedulersProvider
) {
  companion object {
    fun create(
        userSession: UserSession,
        facilityRepository: FacilityRepository,
        patientRepository: PatientRepository,
        patientRegisteredCount: Preference<Int>,
        ui: PatientEntryUi,
        validationActions: PatientEntryValidationActions,
        schedulersProvider: SchedulersProvider
    ): ObservableTransformer<PatientEntryEffect, PatientEntryEvent> {
      return PatientEntryEffectHandler(
          userSession,
          facilityRepository,
          patientRepository,
          patientRegisteredCount,
          ui,
          validationActions,
          schedulersProvider
      ).build()
    }
  }

  private fun build(): ObservableTransformer<PatientEntryEffect, PatientEntryEvent> {
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
        .build()
  }

  private fun fetchOngoingEntryTransformer(scheduler: Scheduler): ObservableTransformer<FetchPatientEntry, PatientEntryEvent> {
    return ObservableTransformer { fetchPatientEntries ->
      val getPatientEntryAndFacility = Singles
          .zip(
              patientRepository.ongoingEntry(),
              facilityRepository.currentFacility(userSession).firstOrError()
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
      showLengthTooLongPhoneNumberError(false)
      showLengthTooShortPhoneNumberError(false)
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
            FULL_NAME_EMPTY -> validationActions.showEmptyFullNameError(true)
            PHONE_NUMBER_LENGTH_TOO_SHORT -> validationActions.showLengthTooShortPhoneNumberError(true)
            PHONE_NUMBER_LENGTH_TOO_LONG -> validationActions.showLengthTooLongPhoneNumberError(true)
            BOTH_DATEOFBIRTH_AND_AGE_ABSENT -> validationActions.showEmptyDateOfBirthAndAgeError(true)
            INVALID_DATE_OF_BIRTH -> validationActions.showInvalidDateOfBirthError(true)
            DATE_OF_BIRTH_IN_FUTURE -> validationActions.showDateOfBirthIsInFutureError(true)
            MISSING_GENDER -> validationActions.showMissingGenderError(true)
            COLONY_OR_VILLAGE_EMPTY -> validationActions.showEmptyColonyOrVillageError(true)
            DISTRICT_EMPTY -> validationActions.showEmptyDistrictError(true)
            STATE_EMPTY -> validationActions.showEmptyStateError(true)
            AGE_EXCEEDS_MAX_LIMIT -> validationActions.showAgeExceedsMaxLimitError(true)
            DOB_EXCEEDS_MAX_LIMIT -> validationActions.showDOBExceedsMaxLimitError(true)
            AGE_EXCEEDS_MIN_LIMIT -> validationActions.showAgeExceedsMinLimitError(true)
            DOB_EXCEEDS_MIN_LIMIT -> validationActions.showDOBExceedsMinLimitError(true)

            EMPTY_ADDRESS_DETAILS,
            PHONE_NUMBER_NON_NULL_BUT_BLANK,
            BOTH_DATEOFBIRTH_AND_AGE_PRESENT,
            PERSONAL_DETAILS_EMPTY -> {
              throw AssertionError("Should never receive this error: $it")
            }
          }
        }

    if (errors.isNotEmpty()) {
      ui.scrollToFirstFieldWithError()
    }
  }
}
