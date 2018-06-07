package org.resolvetosavelives.red.newentry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.newentry.DateOfBirthFormatValidator.Result.INVALID
import org.resolvetosavelives.red.newentry.DateOfBirthFormatValidator.Result.VALID
import org.resolvetosavelives.red.patient.Gender
import org.resolvetosavelives.red.patient.OngoingPatientEntry
import org.resolvetosavelives.red.patient.PatientRepository
import org.resolvetosavelives.red.util.Just
import org.resolvetosavelives.red.util.None
import org.resolvetosavelives.red.util.nullIfBlank
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

typealias UiChange = (PatientEntryScreen) -> Unit
typealias Ui = PatientEntryScreen

class PatientEntryScreenController @Inject constructor(
    private val repository: PatientRepository,
    private val dateOfBirthValidator: DateOfBirthFormatValidator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.mergeArray(
        preFillOnStart(replayedEvents),
        saveButtonToggles(replayedEvents),
        patientSaves(replayedEvents))
  }

  private fun preFillOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .flatMapSingle { repository.ongoingEntry() }
        .map { { ui: Ui -> ui.preFillFields(it) } }
  }

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

    val colonyOrVillageAvailabilities = events
        .ofType<PatientColonyOrVillageTextChanged>()
        .map { it.colonyOrVillage.isNotBlank() }

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

  private fun patientSaves(events: Observable<UiEvent>): Observable<UiChange> {
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
        .ofType<Just<Gender>>()
        .map { it.value }

    val personDetailChanges = Observables.combineLatest(
        nameChanges, dateOfBirthChanges, ageChanges, genderChanges,
        { name, dateOfBirth, age, gender ->
          OngoingPatientEntry.PersonalDetails(name, dateOfBirth.nullIfBlank(), age.nullIfBlank(), gender)
        })

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

    val addressChanges = Observables.combineLatest(
        colonyOrVillageChanges, districtChanges, stateChanges,
        { colonyOrVillage, district, state ->
          OngoingPatientEntry.Address(colonyOrVillage, district, state)
        })

    return events
        .ofType<PatientEntrySaveClicked>()
        .withLatestFrom(
            personDetailChanges,
            phoneNumberChanges,
            addressChanges,
            { _, personal, phone, address -> OngoingPatientEntry(personalDetails = personal, phoneNumber = phone.toNullable(), address = address) })
        .flatMap { entry ->
          repository.saveOngoingEntry(entry)
              .andThen(repository.saveOngoingEntryAsPatient())
              .andThen(Observable.just({ ui: Ui -> ui.openSummaryScreenForBpEntry() }))
        }
  }
}
