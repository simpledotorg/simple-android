package org.simple.clinic.newentry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.Address
import org.simple.clinic.patient.OngoingNewPatientEntry.PersonalDetails
import org.simple.clinic.patient.OngoingNewPatientEntry.PhoneNumber
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
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PatientEntryScreen
typealias UiChange = (Ui) -> Unit

class PatientEntryScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val dobValidator: UserInputDateValidator,
    private val numberValidator: PhoneNumberValidator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    val transformedEvents = replayedEvents
        .mergeWith(ongoingPatientEntryUpdates(replayedEvents))

    return Observable.mergeArray(
        preFillOnStart(transformedEvents),
        switchBetweenDateOfBirthAndAge(transformedEvents),
        toggleDatePatternInDateOfBirthLabel(transformedEvents),
        saveOngoingEntry(transformedEvents),
        savePatient(transformedEvents),
        showValidationErrorsOnSaveClick(transformedEvents),
        resetValidationErrors(transformedEvents),
        scrollToBottomOnGenderSelection(transformedEvents))
  }

  private fun preFillOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .flatMapSingle {
          Singles.zip(
              patientRepository.ongoingEntry(),
              facilityRepository.currentFacility(userSession).firstOrError())
        }
        .map { (entry, facility) ->
          entry.takeIf { it.address != null }
              ?: entry.copy(address = Address(
                  colonyOrVillage = "",
                  district = facility.district,
                  state = facility.state))
        }
        .map { { ui: Ui -> ui.preFillFields(it) } }
  }

  private fun switchBetweenDateOfBirthAndAge(events: Observable<UiEvent>): Observable<UiChange> {
    val isDateOfBirthBlanks = events
        .ofType<PatientDateOfBirthTextChanged>()
        .map { it.dateOfBirth.isBlank() }

    val isAgeBlanks = events
        .ofType<PatientAgeTextChanged>()
        .map { it.age.isBlank() }

    return Observables.combineLatest(isDateOfBirthBlanks, isAgeBlanks)
        .distinctUntilChanged()
        .map<UiChange> { (dateBlank, ageBlank) ->
          when {
            !dateBlank && ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE) }
            dateBlank && !ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(AGE_VISIBLE) }
            dateBlank && ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(BOTH_VISIBLE) }
            else -> throw AssertionError("Both date-of-birth and age cannot have user input at the same time")
          }
        }
  }

  private fun toggleDatePatternInDateOfBirthLabel(events: Observable<UiEvent>): Observable<UiChange> {
    val dateFocusChanges = events
        .ofType<PatientDateOfBirthFocusChanged>()
        .map { it.hasFocus }

    val dateTextAvailabilities = events
        .ofType<PatientDateOfBirthTextChanged>()
        .map { it.dateOfBirth.isNotBlank() }

    return Observables.combineLatest(dateFocusChanges, dateTextAvailabilities)
        .map { (hasFocus, hasDateOfBirth) -> hasFocus || hasDateOfBirth }
        .distinctUntilChanged()
        .map { showPattern -> { ui: Ui -> ui.setShowDatePatternInDateOfBirthLabel(showPattern) } }
  }

  private fun ongoingPatientEntryUpdates(events: Observable<UiEvent>): Observable<OngoingPatientEntryChanged> {
    val nameChanges = events
        .ofType<PatientFullNameTextChanged>()
        .map { it.fullName }

    val dateOfBirthChanges = events
        .ofType<PatientDateOfBirthTextChanged>()
        .map { it.dateOfBirth }

    val ageChanges = events
        .ofType<PatientAgeTextChanged>()
        .map { it.age }

    val genderChanges = events
        .ofType<PatientGenderChanged>()
        .map { it.gender }

    val personDetailChanges = Observables.combineLatest(nameChanges, dateOfBirthChanges, ageChanges, genderChanges)
    { name, dateOfBirth, age, gender ->
      PersonalDetails(name, dateOfBirth.nullIfBlank(), age.nullIfBlank(), gender.toNullable())
    }

    val phoneNumberChanges = events
        .ofType<PatientPhoneNumberTextChanged>()
        .map { (phoneNumber) -> if (phoneNumber.isBlank()) None else Just(PhoneNumber(phoneNumber)) }

    val colonyOrVillageChanges = events
        .ofType<PatientColonyOrVillageTextChanged>()
        .map { it.colonyOrVillage }

    val districtChanges = events
        .ofType<PatientDistrictTextChanged>()
        .map { it.district }

    val stateChanges = events
        .ofType<PatientStateTextChanged>()
        .map { it.state }

    val addressChanges = Observables
        .combineLatest(colonyOrVillageChanges, districtChanges, stateChanges, ::Address)

    return Observables
        .combineLatest(
            personDetailChanges,
            phoneNumberChanges,
            addressChanges
        ) { personal, phone, address ->
          OngoingPatientEntryChanged(OngoingNewPatientEntry(personal, address, phone.toNullable()))
        }
        .flatMapSingle { changedPatientEntry ->
          patientRepository.ongoingEntry()
              .map { alreadyPresentEntry ->
                changedPatientEntry.copy(entry = changedPatientEntry.entry.copy(identifier = alreadyPresentEntry.identifier))
              }
        }
  }

  private fun saveOngoingEntry(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<TheActivityLifecycle.Paused>()
        .withLatestFrom(events.ofType<OngoingPatientEntryChanged>())
        .map { (_, entryUpdate) -> entryUpdate.entry }
        .flatMap { entry ->
          patientRepository.saveOngoingEntry(entry).toObservable<UiChange>()
        }
  }

  private fun showValidationErrorsOnSaveClick(events: Observable<UiEvent>): Observable<UiChange> {
    val errors = events
        .ofType<PatientEntrySaveClicked>()
        .withLatestFrom(events.ofType<OngoingPatientEntryChanged>().map { it.entry })
        .map { (_, ongoingEntry) -> ongoingEntry.validationErrors(dobValidator, numberValidator) }
        .replay(1)
        .refCount()

    val showErrors = errors
        .flatMapIterable { it }
        .doOnNext { Analytics.reportInputValidationError(it.analyticsName) }
        .map {
          val change: UiChange = when (it) {
            FULL_NAME_EMPTY -> { ui: Ui -> ui.showEmptyFullNameError(true) }
            PHONE_NUMBER_LENGTH_TOO_SHORT -> { ui: Ui -> ui.showLengthTooShortPhoneNumberError(true) }
            PHONE_NUMBER_LENGTH_TOO_LONG -> { ui: Ui -> ui.showLengthTooLongPhoneNumberError(true) }
            BOTH_DATEOFBIRTH_AND_AGE_ABSENT -> { ui: Ui -> ui.showEmptyDateOfBirthAndAgeError(true) }
            INVALID_DATE_OF_BIRTH -> { ui: Ui -> ui.showInvalidDateOfBirthError(true) }
            DATE_OF_BIRTH_IN_FUTURE -> { ui: Ui -> ui.showDateOfBirthIsInFutureError(true) }
            MISSING_GENDER -> { ui: Ui -> ui.showMissingGenderError(true) }
            COLONY_OR_VILLAGE_EMPTY -> { ui: Ui -> ui.showEmptyColonyOrVillageError(true) }
            DISTRICT_EMPTY -> { ui: Ui -> ui.showEmptyDistrictError(true) }
            STATE_EMPTY -> { ui: Ui -> ui.showEmptyStateError(true) }

            EMPTY_ADDRESS_DETAILS,
            PHONE_NUMBER_NON_NULL_BUT_BLANK,
            BOTH_DATEOFBIRTH_AND_AGE_PRESENT,
            PERSONAL_DETAILS_EMPTY -> {
              throw AssertionError("Should never receive this error: $it")
            }
          }
          change
        }

    val scrollToFirstError = errors
        .filter { it.isNotEmpty() }
        .map { { ui: Ui -> ui.scrollToFirstFieldWithError() } }

    return showErrors.mergeWith(scrollToFirstError)
  }

  private fun resetValidationErrors(events: Observable<UiEvent>): Observable<UiChange> {
    val nameErrorResets = events
        .ofType<PatientFullNameTextChanged>()
        .map { { ui: Ui -> ui.showEmptyFullNameError(false) } }

    val dateOfBirthErrorResets = events
        .ofType<PatientDateOfBirthTextChanged>()
        .flatMap {
          Observable.just(
              { ui: Ui -> ui.showEmptyDateOfBirthAndAgeError(false) },
              { ui: Ui -> ui.showInvalidDateOfBirthError(false) },
              { ui: Ui -> ui.showDateOfBirthIsInFutureError(false) })
        }

    val ageErrorResets = events
        .ofType<PatientAgeTextChanged>()
        .map { { ui: Ui -> ui.showEmptyDateOfBirthAndAgeError(false) } }

    val genderErrorResets = events
        .ofType<PatientGenderChanged>()
        .map { { ui: Ui -> ui.showMissingGenderError(false) } }

    val phoneLengthTooShortResets = events.ofType<PatientPhoneNumberTextChanged>()
        .map { { ui: Ui -> ui.showLengthTooShortPhoneNumberError(false) } }

    val phoneLengthTooLongResets = events.ofType<PatientPhoneNumberTextChanged>()
        .map { { ui: Ui -> ui.showLengthTooLongPhoneNumberError(false) } }

    val colonyErrorResets = events.ofType<PatientColonyOrVillageTextChanged>()
        .map { { ui: Ui -> ui.showEmptyColonyOrVillageError(false) } }

    val districtErrorResets = events
        .ofType<PatientDistrictTextChanged>()
        .map { { ui: Ui -> ui.showEmptyDistrictError(false) } }

    val stateErrorResets = events
        .ofType<PatientStateTextChanged>()
        .map { { ui: Ui -> ui.showEmptyStateError(false) } }

    return Observable.mergeArray(
        nameErrorResets,
        dateOfBirthErrorResets,
        ageErrorResets,
        genderErrorResets,
        phoneLengthTooShortResets,
        phoneLengthTooLongResets,
        colonyErrorResets,
        districtErrorResets,
        stateErrorResets)
  }

  private fun savePatient(events: Observable<UiEvent>): Observable<UiChange> {
    val ongoingEntryChanges = events
        .ofType<OngoingPatientEntryChanged>()
        .map { it.entry }

    val canPatientBeSaved = ongoingEntryChanges
        .map { it.validationErrors(dobValidator, numberValidator).isEmpty() }

    return events
        .ofType<PatientEntrySaveClicked>()
        .withLatestFrom(ongoingEntryChanges, canPatientBeSaved)
        .flatMapSingle { (_, entry, canBeSaved) ->
          when {
            canBeSaved -> patientRepository.saveOngoingEntry(entry)
                .andThen(Single.just({ ui: Ui -> ui.openMedicalHistoryEntryScreen() }))
            else -> Single.never()
          }
        }
  }

  private fun scrollToBottomOnGenderSelection(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientGenderChanged>()
        .filter { it.gender.isNotEmpty() }
        .take(1)
        .map { { ui: Ui -> ui.scrollFormToBottom() } }
  }
}
