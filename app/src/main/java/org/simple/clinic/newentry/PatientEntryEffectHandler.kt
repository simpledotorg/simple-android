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
import org.simple.clinic.patient.PatientEntryValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.patient.PatientEntryValidationError.BOTH_DATEOFBIRTH_AND_AGE_PRESENT
import org.simple.clinic.patient.PatientEntryValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.patient.PatientEntryValidationError.DISTRICT_EMPTY
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
    private val schedulersProvider: SchedulersProvider
) {
  companion object {
    fun create(
        userSession: UserSession,
        facilityRepository: FacilityRepository,
        patientRepository: PatientRepository,
        patientRegisteredCount: Preference<Int>,
        ui: PatientEntryUi,
        schedulersProvider: SchedulersProvider
    ): ObservableTransformer<PatientEntryEffect, PatientEntryEvent> {
      return PatientEntryEffectHandler(
          userSession,
          facilityRepository,
          patientRepository,
          patientRegisteredCount,
          ui,
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
      FullName -> ui.showEmptyFullNameError(false)
      PhoneNumber -> hidePhoneLengthErrors()
      Age, DateOfBirth -> hideDateOfBirthErrors()
      Gender -> ui.showMissingGenderError(false)
      ColonyOrVillage -> ui.showEmptyColonyOrVillageError(false)
      District -> ui.showEmptyDistrictError(false)
      State -> ui.showEmptyStateError(false)
    }
  }

  private fun hidePhoneLengthErrors() {
    with(ui) {
      showLengthTooLongPhoneNumberError(false)
      showLengthTooShortPhoneNumberError(false)
    }
  }

  private fun hideDateOfBirthErrors() {
    with(ui) {
      showEmptyDateOfBirthAndAgeError(false)
      showInvalidDateOfBirthError(false)
      showDateOfBirthIsInFutureError(false)
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
            FULL_NAME_EMPTY -> ui.showEmptyFullNameError(true)
            PHONE_NUMBER_LENGTH_TOO_SHORT -> ui.showLengthTooShortPhoneNumberError(true)
            PHONE_NUMBER_LENGTH_TOO_LONG -> ui.showLengthTooLongPhoneNumberError(true)
            BOTH_DATEOFBIRTH_AND_AGE_ABSENT -> ui.showEmptyDateOfBirthAndAgeError(true)
            INVALID_DATE_OF_BIRTH -> ui.showInvalidDateOfBirthError(true)
            DATE_OF_BIRTH_IN_FUTURE -> ui.showDateOfBirthIsInFutureError(true)
            MISSING_GENDER -> ui.showMissingGenderError(true)
            COLONY_OR_VILLAGE_EMPTY -> ui.showEmptyColonyOrVillageError(true)
            DISTRICT_EMPTY -> ui.showEmptyDistrictError(true)
            STATE_EMPTY -> ui.showEmptyStateError(true)

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
