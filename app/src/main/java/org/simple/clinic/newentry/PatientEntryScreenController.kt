package org.simple.clinic.newentry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.BOTH_DATEOFBIRTH_AND_AGE_PRESENT
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.COLONY_OR_VILLAGE_NON_NULL_BUT_BLANK
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.DATE_OF_BIRTH_IS_IN_FUTURE
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.DISTRICT_EMPTY
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.EMPTY_ADDRESS_DETAILS
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.FULL_NAME_EMPTY
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.MISSING_GENDER
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.PERSONAL_DETAILS_EMPTY
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.PHONE_NUMBER_NON_NULL_BUT_BLANK
import org.simple.clinic.patient.OngoingPatientEntry.ValidationError.STATE_EMPTY
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.widgets.ActivityLifecycle
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PatientEntryScreen
typealias UiChange = (Ui) -> Unit

class PatientEntryScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    private val dobValidator: DateOfBirthFormatValidator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    val transformedEvents = replayedEvents
        .mergeWith(ongoingPatientEntryUpdates(replayedEvents))

    return Observable.mergeArray(
        preFillOnStart(transformedEvents),
        dateOfBirthAndAgeSwitches(transformedEvents),
        toggleDatePatternInDateOfBirthLabel(transformedEvents),
        ongoingEntrySaves(transformedEvents),
        patientSaves(transformedEvents),
        validationOnSaveClicks(transformedEvents),
        validationErrorResets(transformedEvents),
        noneCheckBoxBehaviors(transformedEvents))
  }

  private fun noneCheckBoxBehaviors(events: Observable<UiEvent>): Observable<UiChange> {
    val noPhoneNumberUnchecks = events
        .ofType<PatientPhoneNumberTextChanged>()
        .map { it.phoneNumber.isNotBlank() }
        .distinctUntilChanged()
        .map { hasPhone -> { ui: Ui -> ui.setNoPhoneNumberCheckboxVisible(!hasPhone) } }

    val noVillageOrColonyUnchecks = events
        .ofType<PatientColonyOrVillageTextChanged>()
        .map { it.colonyOrVillage.isNotBlank() }
        .distinctUntilChanged()
        .map { hasVillageOrColony -> { ui: Ui -> ui.setNoVillageOrColonyCheckboxVisible(!hasVillageOrColony) } }

    val phoneNumberResets = events
        .ofType<PatientNoPhoneNumberToggled>()
        .filter { event -> event.noneSelected }
        .map { { ui: Ui -> ui.resetPhoneNumberField() } }

    val colonyOrVillageResets = events
        .ofType<PatientNoColonyOrVillageToggled>()
        .filter { event -> event.noneSelected }
        .map { { ui: Ui -> ui.resetColonyOrVillageField() } }

    return Observable.mergeArray(
        phoneNumberResets,
        colonyOrVillageResets,
        noPhoneNumberUnchecks,
        noVillageOrColonyUnchecks)
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
          entry.copy(address = OngoingPatientEntry.Address(
              colonyOrVillage = null,
              district = facility.district,
              state = facility.state))
        }
        .map { { ui: Ui -> ui.preFillFields(it) } }
  }

  private fun dateOfBirthAndAgeSwitches(events: Observable<UiEvent>): Observable<UiChange> {
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
            else -> throw AssertionError("Both date-of-birth and age cannot have loggedInUser input at the same time")
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
      OngoingPatientEntry.PersonalDetails(name, dateOfBirth.nullIfBlank(), age.nullIfBlank(), gender.toNullable())
    }

    val phoneNumberChanges = Observables
        .combineLatest(
            events
                .ofType<PatientNoPhoneNumberToggled>()
                .map { it.noneSelected },
            events
                .ofType<PatientPhoneNumberTextChanged>()
                .map { it.phoneNumber })
        .map { (noneSelected, phoneNumber) ->
          when {
            noneSelected -> None
            phoneNumber.isBlank() -> None
            else -> Just(OngoingPatientEntry.PhoneNumber(phoneNumber!!))
          }
        }

    val colonyOrVillageChanges = Observables
        .combineLatest(
            events
                .ofType<PatientNoColonyOrVillageToggled>()
                .map { it.noneSelected },
            events
                .ofType<PatientColonyOrVillageTextChanged>()
                .map { it.colonyOrVillage })
        .map { (noneSelected, colonyOrVillage) ->
          when {
            noneSelected -> None
            else -> Just(colonyOrVillage!!)
          }
        }

    val districtChanges = events
        .ofType<PatientDistrictTextChanged>()
        .map { it.district }

    val stateChanges = events
        .ofType<PatientStateTextChanged>()
        .map { it.state }

    val addressChanges = Observables.combineLatest(colonyOrVillageChanges, districtChanges, stateChanges)
    { colonyOrVillage, district, state ->
      OngoingPatientEntry.Address(colonyOrVillage.toNullable().nullIfBlank(), district, state)
    }

    return Observables.combineLatest(personDetailChanges, phoneNumberChanges, addressChanges)
    { personal, phone, address ->
      OngoingPatientEntryChanged(OngoingPatientEntry(personal, address, phone.toNullable()))
    }
  }

  private fun ongoingEntrySaves(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ActivityLifecycle.Paused>()
        .withLatestFrom(events.ofType<OngoingPatientEntryChanged>())
        .map { (_, entryUpdate) -> entryUpdate.entry }
        .flatMap { entry ->
          patientRepository.saveOngoingEntry(entry).toObservable<UiChange>()
        }
  }

  private fun validationOnSaveClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val showDataErrors = events
        .ofType<PatientEntrySaveClicked>()
        .withLatestFrom(events.ofType<OngoingPatientEntryChanged>().map { it.entry })
        .map { (_, ongoingEntry) ->
          ongoingEntry.validationErrors(dobValidator)
              .map {
                val change: UiChange = when (it) {
                  FULL_NAME_EMPTY -> { ui: Ui -> ui.showEmptyFullNameError(true) }
                  BOTH_DATEOFBIRTH_AND_AGE_ABSENT -> { ui: Ui -> ui.showEmptyDateOfBirthAndAgeError(true) }
                  INVALID_DATE_OF_BIRTH -> { ui: Ui -> ui.showInvalidDateOfBirthError(true) }
                  DATE_OF_BIRTH_IS_IN_FUTURE -> { ui: Ui -> ui.showDateOfBirthIsInFutureError(true) }
                  MISSING_GENDER -> { ui: Ui -> ui.showMissingGenderError(true) }
                  DISTRICT_EMPTY -> { ui: Ui -> ui.showEmptyDistrictError(true) }
                  STATE_EMPTY -> { ui: Ui -> ui.showEmptyStateError(true) }

                  EMPTY_ADDRESS_DETAILS,
                  PHONE_NUMBER_NON_NULL_BUT_BLANK,
                  BOTH_DATEOFBIRTH_AND_AGE_PRESENT,
                  COLONY_OR_VILLAGE_NON_NULL_BUT_BLANK,
                  PERSONAL_DETAILS_EMPTY -> {
                    throw AssertionError("Should never receive this error: $it")
                  }
                }
                change
              }
        }
        .flatMapIterable { it }

    val phoneNumberChanges = events.ofType<PatientPhoneNumberTextChanged>().map { it.phoneNumber }
    val noPhoneToggles = events.ofType<PatientNoPhoneNumberToggled>().map { it.noneSelected }

    val showPhoneNumberErrors = events
        .ofType<PatientEntrySaveClicked>()
        .withLatestFrom(phoneNumberChanges, noPhoneToggles)
        .filter { (_, phoneNumber, isNoneSelected) -> phoneNumber.isBlank() && !isNoneSelected }
        .map { { ui: Ui -> ui.showEmptyPhoneNumberError(true) } }

    val colonyChanges = events.ofType<PatientColonyOrVillageTextChanged>().map { it.colonyOrVillage }
    val noColonyToggles = events.ofType<PatientNoColonyOrVillageToggled>().map { it.noneSelected }

    val colonyValidations = events
        .ofType<PatientEntrySaveClicked>()
        .withLatestFrom(colonyChanges, noColonyToggles)
        .filter { (_, colony, isNoneSelected) -> colony.isBlank() && !isNoneSelected }
        .map { { ui: Ui -> ui.showEmptyColonyOrVillageError(true) } }

    return showDataErrors
        .mergeWith(showPhoneNumberErrors)
        .mergeWith(colonyValidations)
  }

  private fun validationErrorResets(events: Observable<UiEvent>): Observable<UiChange> {
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

    val phoneErrorResets = Observable
        .merge(events.ofType<PatientPhoneNumberTextChanged>(), events.ofType<PatientNoPhoneNumberToggled>())
        .map { { ui: Ui -> ui.showEmptyPhoneNumberError(false) } }

    val colonyErrorResets = Observable
        .merge(events.ofType<PatientColonyOrVillageTextChanged>(), events.ofType<PatientNoColonyOrVillageToggled>())
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
        phoneErrorResets,
        colonyErrorResets,
        districtErrorResets,
        stateErrorResets)
  }

  private fun patientSaves(events: Observable<UiEvent>): Observable<UiChange> {
    val phoneNumberChanges = events.ofType<PatientPhoneNumberTextChanged>().map { it.phoneNumber }
    val noPhoneToggles = events.ofType<PatientNoPhoneNumberToggled>().map { it.noneSelected }
    val colonyChanges = events.ofType<PatientColonyOrVillageTextChanged>().map { it.colonyOrVillage }
    val noColonyToggles = events.ofType<PatientNoColonyOrVillageToggled>().map { it.noneSelected }

    val isPhoneNumberValid = events
        .ofType<PatientEntrySaveClicked>()
        .withLatestFrom(phoneNumberChanges, noPhoneToggles)
        .map { (_, phoneNumber, isNoneSelected) -> phoneNumber.isNotBlank() || isNoneSelected }

    val isColonyValid = events
        .ofType<PatientEntrySaveClicked>()
        .withLatestFrom(colonyChanges, noColonyToggles)
        .map { (_, colony, isNoneSelected) -> colony.isNotBlank() || isNoneSelected }

    val ongoingEntryChanges = events
        .ofType<OngoingPatientEntryChanged>()
        .map { it.entry }

    val canPatientBeSaved = Observables
        .combineLatest(
            ongoingEntryChanges.map { it.validationErrors(dobValidator).isEmpty() },
            isPhoneNumberValid,
            isColonyValid)
        .map { it.first.and(it.second).and(it.third) }

    return events
        .ofType<PatientEntrySaveClicked>()
        .withLatestFrom(ongoingEntryChanges, canPatientBeSaved)
        .flatMapSingle { (_, entry, canBeSaved) ->
          when {
            canBeSaved -> patientRepository.saveOngoingEntry(entry)
                .andThen(patientRepository.saveOngoingEntryAsPatient())
                .map { savedPatient -> { ui: Ui -> ui.openSummaryScreenForBpEntry(savedPatient.uuid) } }
            else -> Single.never()
          }
        }
  }
}
