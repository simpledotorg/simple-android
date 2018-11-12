package org.simple.clinic.editpatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.OngoingEditPatientEntry
import org.simple.clinic.patient.Patient
import org.simple.clinic.editpatient.PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.PatientEditValidationError.STATE_EMPTY
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PatientEditScreen
typealias UiChange = (Ui) -> Unit

class PatientEditScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val numberValidator: PhoneNumberValidator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    val transformedEvents = replayedEvents.mergeWith(ongoingEntryPatientEntryChanges(replayedEvents))

    return Observable.merge(
        prefillOnStart(transformedEvents),
        showValidationErrorsOnSaveClick(transformedEvents),
        hideValidationErrorsOnInput(transformedEvents),
        savePatientDetails(transformedEvents))
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

    return Observable.merge(preFillPatientProfile, preFillPhoneNumber, preFillPatientAddress)
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
    val errorsFromPhoneNumber = events
        .ofType<PatientEditPhoneNumberTextChanged>()
        .map { setOf(PHONE_NUMBER_EMPTY, PHONE_NUMBER_LENGTH_TOO_LONG, PHONE_NUMBER_LENGTH_TOO_SHORT) }

    val errorsFromName = events
        .ofType<PatientEditPatientNameTextChanged>()
        .map { setOf(FULL_NAME_EMPTY) }

    val errorsFromColonyOrVillage = events
        .ofType<PatientEditColonyOrVillageChanged>()
        .map { setOf(COLONY_OR_VILLAGE_EMPTY) }

    val errorsFromState = events
        .ofType<PatientEditStateTextChanged>()
        .map { setOf(STATE_EMPTY) }

    val errorsFromDistrict = events
        .ofType<PatientEditDistrictTextChanged>()
        .map { setOf(DISTRICT_EMPTY) }

    return Observable
        .mergeArray(
            errorsFromPhoneNumber,
            errorsFromName,
            errorsFromColonyOrVillage,
            errorsFromState,
            errorsFromDistrict
        )
        .map { errors ->
          { ui: Ui -> ui.hideValidationErrors(errors) }
        }
  }

  private fun savePatientDetails(events: Observable<UiEvent>): Observable<UiChange> {
    val saveClicks = events.ofType<PatientEditSaveClicked>()

    val patientUuidStream = events.ofType<PatientEditScreenCreated>()
        .map { it.patientUuid }

    val ongoingGoingEditPatientEntryChanges = events.ofType<OngoingEditPatientEntryChanged>()
        .map { it.ongoingEditPatientEntry }

    val savedPhoneNumber = patientUuidStream
        .flatMap { patientRepository.phoneNumbers(it) }
        .take(1)
        .replay()
        .refCount()

    // ongoingGoingEditPatientEntryChanges keeps emitting as and when the user types. We use the save clicks
    // here because we want to take the patient data at the moment the save button was clicked.
    val validEditPatientEntry = saveClicks
        .withLatestFrom(ongoingGoingEditPatientEntryChanges) { _, ongoingEditPatientEntry -> ongoingEditPatientEntry }
        .withLatestFrom(savedPhoneNumber)
        .filter { (ongoingEditPatientEntry, savedPhoneNumber) ->
          ongoingEditPatientEntry.validate(savedPhoneNumber.toNullable(), numberValidator).isEmpty()
        }
        .map { (ongoingEditPatientEntry) -> ongoingEditPatientEntry }

    val updateExistingPhoneNumber = validEditPatientEntry
        .withLatestFrom(savedPhoneNumber)
        .filter { (_, savedPhoneNumber) -> savedPhoneNumber is Just }
        .map { (ongoingEditPatientEntry, savedPhoneNumber) ->
          (savedPhoneNumber as Just).value.copy(number = ongoingEditPatientEntry.phoneNumber)
        }
        .flatMapSingle { updatedPhoneNumber ->
          patientRepository
              .updatePhoneNumberForPatient(updatedPhoneNumber.patientUuid, updatedPhoneNumber)
              .toSingleDefault(true)
        }

    val saveNewPhoneNumber = validEditPatientEntry
        .withLatestFrom(savedPhoneNumber)
        .filter { (_, savedPhoneNumber) -> savedPhoneNumber is None }
        .withLatestFrom(patientUuidStream) { (ongoingEditPatientEntry, _), patientUuid -> patientUuid to ongoingEditPatientEntry }
        .flatMapSingle { (patientUuid, ongoingEditPatientEntry) ->
          patientRepository
              .savePhoneNumberForPatient(patientUuid, ongoingEditPatientEntry.phoneNumber, PatientPhoneNumberType.MOBILE, true)
              .toSingleDefault(true)
        }

    val saveOrUpdatePhoneNumber = Observable.ambArray(saveNewPhoneNumber, updateExistingPhoneNumber)

    val savedPatient = patientUuidStream
        .flatMap { patientRepository.patient(it).take(1).unwrapJust() }
        .replay()
        .refCount()

    val savedPatientAddress = savedPatient
        .flatMap { patientRepository.address(it.addressUuid).take(1).unwrapJust() }
        .replay()
        .refCount()

    val savePatientDetails = validEditPatientEntry
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
}
