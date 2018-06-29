package org.simple.clinic.newentry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result.INVALID
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result.VALID
import org.simple.clinic.patient.OngoingPatientEntry
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
    private val dateOfBirthValidator: DateOfBirthFormatValidator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    val transformedEvents = replayedEvents
        .mergeWith(ongoingPatientEntryUpdates(replayedEvents))

    return Observable.mergeArray(
        preFillOnStart(transformedEvents),
        saveButtonToggles(transformedEvents),
        dateOfBirthAndAgeSwitches(transformedEvents),
        toggleDatePatternInDateOfBirthLabel(transformedEvents),
        ongoingEntrySaves(transformedEvents),
        patientSaves(transformedEvents),
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
        .flatMapSingle { patientRepository.ongoingEntry() }
        .withLatestFrom(facilityRepository.currentFacility(userSession).take(1))
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

  // TODO: Instead of checking each field, can we use OngoingPatientEntryChanged event and just call canBeSaved() on it?
  private fun saveButtonToggles(events: Observable<UiEvent>): Observable<UiChange> {
    val nameAvailabilities = events
        .ofType<PatientFullNameTextChanged>()
        .map { it.fullName.isNotBlank() }

    val phoneAvailabilities = Observables
        .combineLatest(
            events
                .ofType<PatientNoPhoneNumberToggled>()
                .map { it.noneSelected },
            events
                .ofType<PatientPhoneNumberTextChanged>()
                .map { it.phoneNumber.isNotBlank() })
        .map { (noneSelected, phoneAvailable) -> noneSelected || phoneAvailable }

    val dateOfBirthAvailabilities = events
        .ofType<PatientDateOfBirthTextChanged>()
        .map { dateOfBirthValidator.validate(it.dateOfBirth) }
        .map {
          when (it) {
            VALID -> true
            INVALID -> false
          }
        }

    val ageAvailabilities = events
        .ofType<PatientAgeTextChanged>()
        .map { it.age.isNotBlank() }

    val dateOfBirthOrAgeAvailabilities = Observables
        .combineLatest(dateOfBirthAvailabilities, ageAvailabilities)
        .map { (dobAvailable, ageAvailable) -> dobAvailable || ageAvailable }

    val genderAvailabilities = events
        .ofType<PatientGenderChanged>()
        .map {
          when (it.gender) {
            is Just -> true
            is None -> false
          }
        }

    val colonyOrVillageAvailabilities = Observables
        .combineLatest(
            events
                .ofType<PatientNoColonyOrVillageToggled>()
                .map { it.noneSelected },
            events
                .ofType<PatientColonyOrVillageTextChanged>()
                .map { it.colonyOrVillage.isNotBlank() })
        .map { (noneSelected, colonyAvailable) -> noneSelected || colonyAvailable }

    val districtAvailabilities = events
        .ofType<PatientDistrictTextChanged>()
        .map { it.district.isNotBlank() }

    val stateAvailabilities = events
        .ofType<PatientStateTextChanged>()
        .map { it.state.isNotBlank() }

    val addressAvailabilities = Observables
        .combineLatest(colonyOrVillageAvailabilities, districtAvailabilities, stateAvailabilities)
        .map { it.first && it.second && it.third }

    val requireAllFunction: (Boolean, Boolean, Boolean, Boolean, Boolean) -> Boolean =
        { bool1, bool2, bol3, bool4, bool5 ->
          bool1 && bool2 && bol3 && bool4 && bool5
        }

    return Observables
        .combineLatest(
            nameAvailabilities,
            phoneAvailabilities,
            dateOfBirthOrAgeAvailabilities,
            genderAvailabilities,
            addressAvailabilities,
            requireAllFunction)
        .distinctUntilChanged()
        .map { allDetailsAvailable ->
          { ui: Ui -> ui.setSaveButtonEnabled(allDetailsAvailable) }
        }
  }

  private fun ongoingPatientEntryUpdates(events: Observable<UiEvent>): Observable<UiEvent> {
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
            else -> Just(OngoingPatientEntry.PhoneNumber(phoneNumber!!))
          }
        }

    val colonyOrVillageChanges = events
        .ofType<PatientColonyOrVillageTextChanged>()
        .map { it.colonyOrVillage }

    val districtChanges = events
        .ofType<PatientDistrictTextChanged>()
        .map { it.district }

    val stateChanges = events
        .ofType<PatientStateTextChanged>()
        .map { it.state }

    val addressChanges = Observables.combineLatest(colonyOrVillageChanges, districtChanges, stateChanges)
    { colonyOrVillage, district, state ->
      OngoingPatientEntry.Address(colonyOrVillage.nullIfBlank(), district, state)
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

  private fun patientSaves(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientEntrySaveClicked>()
        .withLatestFrom(events.ofType<OngoingPatientEntryChanged>())
        .map { (_, entryUpdate) -> entryUpdate.entry }
        .flatMapSingle { entry ->
          patientRepository.saveOngoingEntry(entry)
              .andThen(patientRepository.saveOngoingEntryAsPatient())
              .map { savedPatient -> { ui: Ui -> ui.openSummaryScreenForBpEntry(savedPatient.uuid) } }
        }
  }
}
