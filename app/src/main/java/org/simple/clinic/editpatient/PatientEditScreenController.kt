package org.simple.clinic.editpatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.editpatient.PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.PatientEditValidationError.STATE_EMPTY
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility
import org.simple.clinic.patient.OngoingEditPatientEntry
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.None
import org.simple.clinic.util.estimateCurrentAge
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Clock
import javax.inject.Inject

typealias Ui = PatientEditScreen
typealias UiChange = (Ui) -> Unit

class PatientEditScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val numberValidator: PhoneNumberValidator,
    private val configProvider: Single<PatientEditConfig>,
    private val clock: Clock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    val transformedEvents = replayedEvents.mergeWith(ongoingEntryPatientEntryChanges(replayedEvents))

    return Observable.mergeArray(
        prefillOnStart(transformedEvents),
        showValidationErrorsOnSaveClick(transformedEvents),
        hideValidationErrorsOnInput(transformedEvents),
        savePatientDetails(transformedEvents),
        toggleEditAgeAndDateofBirthFeature(transformedEvents),
        toggleDatePatternInDateOfBirthLabel(transformedEvents),
        switchBetweenDateOfBirthAndAge(transformedEvents))
  }

  private fun toggleEditAgeAndDateofBirthFeature(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientEditScreenCreated>()
        .flatMapSingle { configProvider }
        .map { (isEditAgeAndDobEnabled) ->
          { ui: Ui ->
            if (isEditAgeAndDobEnabled) {
              ui.enableEditAgeAndDateOfBirthFeature()
            } else {
              ui.disableEditAgeAndDateOfBirthFeature()
            }
          }
        }
  }

  private fun prefillOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuidStream = events.ofType<PatientEditScreenCreated>()
        .map { it.patientUuid }

    val savedPatient = patientUuidStream
        .flatMap(patientRepository::patient)
        .take(1)
        .unwrapJust()
        .replay()
        .refCount()

    val savedAddress = savedPatient
        .flatMap { patient ->
          patientRepository
              .address(patient.addressUuid)
              .take(1)
              .unwrapJust()
        }

    val preFillPatientProfile = savedPatient
        .map { patient: Patient ->
          { ui: Ui ->
            ui.setPatientName(patient.fullName)
            ui.setGender(patient.gender)
          }
        }

    val preFillPhoneNumber = savedPatient
        .flatMap { patientRepository.phoneNumbers(it.uuid) }
        .take(1)
        .filterAndUnwrapJust()
        .map { phoneNumber ->
          { ui: Ui ->
            ui.setPatientPhoneNumber(phoneNumber.number)
          }
        }

    val preFillPatientAddress = savedAddress
        .map { address ->
          { ui: Ui ->
            ui.setState(address.state)
            ui.setDistrict(address.district)

            if (address.colonyOrVillage.isNullOrBlank().not()) {
              ui.setColonyOrVillage(address.colonyOrVillage!!)
            }
          }
        }

    val prefillPatientAge = savedPatient
        .filter { it.age != null }
        .map {
          { ui: Ui ->
            val estimatedAge = estimateCurrentAge(it.age!!.value, it.age.updatedAt, clock)
            ui.setPatientAge(estimatedAge)
          }
        }

    val prefillPatientDateOfBirth = savedPatient
        .filter { it.dateOfBirth != null }
        .map { { ui: Ui -> ui.setPatientDateofBirth(it.dateOfBirth!!) } }

    return Observable.mergeArray(
        preFillPatientProfile,
        preFillPhoneNumber,
        preFillPatientAddress,
        prefillPatientAge,
        prefillPatientDateOfBirth)
  }

  private fun ongoingEntryPatientEntryChanges(events: Observable<UiEvent>): Observable<UiEvent> {
    val nameChanges = events
        .ofType<PatientEditPatientNameTextChanged>()
        .map { it.name }

    val genderChanges = events
        .ofType<PatientEditGenderChanged>()
        .map { it.gender }

    val colonyOrVillageChanges = events
        .ofType<PatientEditColonyOrVillageChanged>()
        .map { it.colonyOrVillage }

    val districtChanges = events
        .ofType<PatientEditDistrictTextChanged>()
        .map { it.district }

    val stateChanges = events
        .ofType<PatientEditStateTextChanged>()
        .map { it.state }

    val phoneNumberChanges = events
        .ofType<PatientEditPhoneNumberTextChanged>()
        .map { it.phoneNumber }

    return Observables.combineLatest(
        nameChanges,
        genderChanges,
        colonyOrVillageChanges,
        districtChanges,
        stateChanges,
        phoneNumberChanges
    ) { name, gender, colonyOrVillage, district, state, phoneNumber ->
      OngoingEditPatientEntryChanged(OngoingEditPatientEntry(name, gender, phoneNumber, colonyOrVillage, district, state))
    }
  }

  private fun showValidationErrorsOnSaveClick(events: Observable<UiEvent>): Observable<UiChange> {
    val ongoingEditPatientEntryChanges = events.ofType<OngoingEditPatientEntryChanged>()

    val savedPhoneNumber = events.ofType<PatientEditScreenCreated>()
        .flatMap { patientRepository.phoneNumbers(it.patientUuid) }
        .take(1)
        .replay()
        .refCount()

    return events.ofType<PatientEditSaveClicked>()
        .withLatestFrom(ongoingEditPatientEntryChanges, savedPhoneNumber) { _, (ongoingEditPatientEntry), phoneNumber ->
          ongoingEditPatientEntry to phoneNumber
        }
        .map { (ongoingEditPatientEntry, phoneNumber) ->
          ongoingEditPatientEntry.validate(phoneNumber.toNullable(), numberValidator)
        }
        .filter { it.isNotEmpty() }
        .map { errors ->
          { ui: Ui ->
            ui.showValidationErrors(errors)
            ui.scrollToFirstFieldWithError()
          }
        }
  }

  private fun hideValidationErrorsOnInput(events: Observable<UiEvent>): Observable<UiChange> {
    val errorsForEventType = mapOf(
        PatientEditPhoneNumberTextChanged::class to setOf(PHONE_NUMBER_EMPTY, PHONE_NUMBER_LENGTH_TOO_LONG, PHONE_NUMBER_LENGTH_TOO_SHORT),
        PatientEditPatientNameTextChanged::class to setOf(FULL_NAME_EMPTY),
        PatientEditColonyOrVillageChanged::class to setOf(COLONY_OR_VILLAGE_EMPTY),
        PatientEditStateTextChanged::class to setOf(STATE_EMPTY),
        PatientEditDistrictTextChanged::class to setOf(DISTRICT_EMPTY))

    return events
        .map { uiEvent -> errorsForEventType[uiEvent::class] ?: emptySet() }
        .filter { it.isNotEmpty() }
        .map { errors -> { ui: Ui -> ui.hideValidationErrors(errors) } }
  }

  private fun savePatientDetails(events: Observable<UiEvent>): Observable<UiChange> {
    val saveClicks = events.ofType<PatientEditSaveClicked>()

    val patientUuidStream = events.ofType<PatientEditScreenCreated>()
        .map { it.patientUuid }

    val entryChanges = events.ofType<OngoingEditPatientEntryChanged>()
        .map { it.ongoingEditPatientEntry }

    val savedNumbers = patientUuidStream
        .flatMap { patientRepository.phoneNumbers(it) }
        .take(1)
        .replay()
        .refCount()

    val validEntry = saveClicks
        .withLatestFrom(entryChanges, savedNumbers)
        .filter { (_, entry, savedNumber) ->
          val validationErrors = entry.validate(savedNumber.toNullable(), numberValidator)
          validationErrors.isEmpty()
        }
        .map { (_, entry) -> entry }

    val updateExistingPhoneNumber = validEntry
        .withLatestFrom(savedNumbers.filterAndUnwrapJust())
        .map { (entry, savedNumber) -> savedNumber.copy(number = entry.phoneNumber) }
        .flatMapSingle { updatedNumber ->
          patientRepository
              .updatePhoneNumberForPatient(updatedNumber.patientUuid, updatedNumber)
              //Doing this because Completables are not working properly
              .toSingleDefault(true)
        }

    val createNewPhoneNumber = validEntry
        .withLatestFrom(savedNumbers.ofType<None>(), patientUuidStream)
        .flatMapSingle { (entry, _, patientUuid) ->
          patientRepository
              .createPhoneNumberForPatient(patientUuid, entry.phoneNumber, phoneNumberType = PatientPhoneNumberType.MOBILE, active = true)
              .toSingleDefault(true)
        }

    val saveOrUpdatePhoneNumber = Observable.merge(createNewPhoneNumber, updateExistingPhoneNumber)

    val savedPatient = patientUuidStream
        .flatMap { patientRepository.patient(it).take(1).unwrapJust() }
        .replay()
        .refCount()

    val savedPatientAddress = savedPatient
        .flatMap { patientRepository.address(it.addressUuid).take(1).unwrapJust() }
        .replay()
        .refCount()

    val savePatientDetails = validEntry
        .withLatestFrom(savedPatient, savedPatientAddress)
        .map { (ongoingEditPatientEntry, patient, patientAddress) ->
          val updatedPatient = patient.copy(
              fullName = ongoingEditPatientEntry.name,
              gender = ongoingEditPatientEntry.gender
          )

          val updatedAddress = patientAddress.copy(
              colonyOrVillage = ongoingEditPatientEntry.colonyOrVillage,
              state = ongoingEditPatientEntry.state,
              district = ongoingEditPatientEntry.district
          )

          updatedPatient to updatedAddress
        }
        .flatMapSingle { (updatedPatient, updatedPatientAddress) ->
          patientRepository.updatePatient(updatedPatient)
              .andThen(patientRepository.updateAddressForPatient(updatedPatient.uuid, updatedPatientAddress))
              .toSingleDefault(true)
        }

    return Observables.zip(savePatientDetails, saveOrUpdatePhoneNumber)
        .filter { (patientSaved, numberSaved) -> patientSaved && numberSaved }
        .map { (_, _) -> { ui: Ui -> ui.goBack() } }
  }

  private fun toggleDatePatternInDateOfBirthLabel(events: Observable<UiEvent>): Observable<UiChange> {
    val dateFocusChanges = events
        .ofType<PatientEditDateOfBirthFocusChanged>()
        .map { it.hasFocus }

    val dateTextAvailabilities = events
        .ofType<PatientEditDateOfBirthTextChanged>()
        .map { it.dateOfBirth.isNotBlank() }

    return Observables.combineLatest(dateFocusChanges, dateTextAvailabilities)
        .map { (hasFocus, hasDateOfBirth) -> hasFocus || hasDateOfBirth }
        .distinctUntilChanged()
        .map { showPattern ->
          { ui: Ui ->
            if (showPattern) {
              ui.showDatePatternInDateOfBirthLabel()

            } else {
              ui.hideDatePatternInDateOfBirthLabel()
            }
          }
        }
  }

  private fun switchBetweenDateOfBirthAndAge(events: Observable<UiEvent>): Observable<UiChange> {
    val isDateOfBirthBlanks = events
        .ofType<PatientEditDateOfBirthTextChanged>()
        .map { it.dateOfBirth.isBlank() }

    val isAgeBlanks = events
        .ofType<PatientEditAgeTextChanged>()
        .map { it.age.isBlank() }

    return Observables.combineLatest(isDateOfBirthBlanks, isAgeBlanks)
        .distinctUntilChanged()
        .map<UiChange> { (dateBlank, ageBlank) ->
          when {
            !dateBlank && ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE) }
            dateBlank && !ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.AGE_VISIBLE) }
            dateBlank && ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.BOTH_VISIBLE) }
            else -> throw AssertionError("Both date-of-birth and age cannot have user input at the same time")
          }
        }
  }
}
