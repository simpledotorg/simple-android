package org.simple.clinic.editpatient_old

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.editpatient.AgeChanged
import org.simple.clinic.editpatient.ColonyOrVillageChanged
import org.simple.clinic.editpatient.DateOfBirthChanged
import org.simple.clinic.editpatient.DateOfBirthFocusChanged
import org.simple.clinic.editpatient.DistrictChanged
import org.simple.clinic.editpatient.EditPatientScreenCreated
import org.simple.clinic.editpatient.EditPatientUi
import org.simple.clinic.editpatient.EditPatientValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.editpatient.EditPatientValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.editpatient.EditPatientValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.EditPatientValidationError.STATE_EMPTY
import org.simple.clinic.editpatient.GenderChanged
import org.simple.clinic.editpatient.NameChanged
import org.simple.clinic.editpatient.OngoingEditPatientEntry
import org.simple.clinic.editpatient.OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.editpatient.PatientEditBackClicked
import org.simple.clinic.editpatient.PhoneNumberChanged
import org.simple.clinic.editpatient.SaveClicked
import org.simple.clinic.editpatient.StateChanged
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.None
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.mapType
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

typealias Ui = EditPatientUi
typealias UiChange = (Ui) -> Unit

class PatientEditScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val numberValidator: PhoneNumberValidator,
    private val utcClock: UtcClock,
    private val userClock: UserClock,
    private val dobValidator: UserInputDateValidator,
    @Named("date_for_user_input") private val dateOfBirthFormatter: DateTimeFormatter
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(mergeWithOngoingEntryPatientEntryChanges())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        prefillOnStart(replayedEvents),
        showValidationErrorsOnSaveClick(replayedEvents),
        hideValidationErrorsOnInput(replayedEvents),
        savePatientDetails(replayedEvents),
        toggleDatePatternInDateOfBirthLabel(replayedEvents),
        switchBetweenDateOfBirthAndAge(replayedEvents),
        closeScreenWithoutSaving(replayedEvents))
  }

  private fun prefillOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<EditPatientScreenCreated>()
        .take(1)
        .map { (patient, address, phoneNumber) ->
          { ui: Ui -> prefillFormFields(ui, patient, phoneNumber, address) }
        }
  }

  private fun prefillFormFields(ui: Ui, patient: Patient, phoneNumber: PatientPhoneNumber?, address: PatientAddress) {
    ui.setPatientName(patient.fullName)
    ui.setGender(patient.gender)
    phoneNumber?.let { ui.setPatientPhoneNumber(it.number) }
    ui.setState(address.state)
    ui.setDistrict(address.district)

    if (address.colonyOrVillage.isNullOrBlank().not()) {
      ui.setColonyOrVillage(address.colonyOrVillage!!)
    }

    val dateOfBirth = DateOfBirth.fromPatient(patient, userClock)
    when (dateOfBirth.type) {
      DateOfBirth.Type.EXACT -> ui.setPatientDateOfBirth(dateOfBirth.date)
      DateOfBirth.Type.FROM_AGE -> ui.setPatientAge(dateOfBirth.estimateAge(userClock))
    }
  }

  private fun mergeWithOngoingEntryPatientEntryChanges(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val eitherAgeOrDateOfBirthChanges = Observable.merge(
          events.mapType<AgeChanged, EntryWithAge> { EntryWithAge(it.age.trim()) },
          events.mapType<DateOfBirthChanged, EntryWithDateOfBirth> { EntryWithDateOfBirth(it.dateOfBirth.trim()) }
      )

      val ongoingEntryChanges = Observables.combineLatest(
          events.mapType<EditPatientScreenCreated, UUID> { it.patient.uuid },
          events.mapType<NameChanged, String> { it.name.trim() },
          events.mapType<GenderChanged, Gender> { it.gender },
          events.mapType<ColonyOrVillageChanged, String> { it.colonyOrVillage.trim() },
          events.mapType<DistrictChanged, String> { it.district.trim() },
          events.mapType<StateChanged, String> { it.state.trim() },
          events.mapType<PhoneNumberChanged, String> { it.phoneNumber.trim() },
          eitherAgeOrDateOfBirthChanges
      ) { patientUuid, name, gender, colonyOrVillage, district, state, phoneNumber, ageOrDateOfBirth ->
        OngoingEditPatientEntryChanged(OngoingEditPatientEntry(
            patientUuid = patientUuid,
            name = name,
            gender = gender,
            phoneNumber = phoneNumber,
            colonyOrVillage = colonyOrVillage,
            district = district,
            state = state,
            ageOrDateOfBirth = ageOrDateOfBirth))
      }

      events.mergeWith(ongoingEntryChanges)
    }
  }

  private fun showValidationErrorsOnSaveClick(events: Observable<UiEvent>): Observable<UiChange> {
    val ongoingEditPatientEntryChanges = events.ofType<OngoingEditPatientEntryChanged>()

    val savedPhoneNumber = events.ofType<EditPatientScreenCreated>()
        .map { it.phoneNumber.toOptional() }
        .take(1)
        .replay()
        .refCount()

    return events.ofType<SaveClicked>()
        .withLatestFrom(ongoingEditPatientEntryChanges, savedPhoneNumber) { _, (ongoingEditPatientEntry), phoneNumber ->
          ongoingEditPatientEntry to phoneNumber
        }
        .map { (ongoingEditPatientEntry, phoneNumber) ->
          ongoingEditPatientEntry.validate(phoneNumber.toNullable(), numberValidator, dobValidator)
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
        PhoneNumberChanged::class to setOf(PHONE_NUMBER_EMPTY, PHONE_NUMBER_LENGTH_TOO_LONG, PHONE_NUMBER_LENGTH_TOO_SHORT),
        NameChanged::class to setOf(FULL_NAME_EMPTY),
        ColonyOrVillageChanged::class to setOf(COLONY_OR_VILLAGE_EMPTY),
        StateChanged::class to setOf(STATE_EMPTY),
        DistrictChanged::class to setOf(DISTRICT_EMPTY),
        AgeChanged::class to setOf(BOTH_DATEOFBIRTH_AND_AGE_ABSENT),
        DateOfBirthChanged::class to setOf(INVALID_DATE_OF_BIRTH, DATE_OF_BIRTH_IN_FUTURE))

    return events
        .map { uiEvent -> errorsForEventType[uiEvent::class] ?: emptySet() }
        .filter { it.isNotEmpty() }
        .map { errors -> { ui: Ui -> ui.hideValidationErrors(errors) } }
  }

  private fun savePatientDetails(events: Observable<UiEvent>): Observable<UiChange> {
    val saveClicks = events.ofType<SaveClicked>()

    val screenCreatedStream = events
        .ofType<EditPatientScreenCreated>()

    val patientUuidStream = screenCreatedStream
        .map { it.patient.uuid }

    val entryChanges = events.ofType<OngoingEditPatientEntryChanged>()
        .map { it.ongoingEditPatientEntry }

    val savedNumbers = screenCreatedStream
        .map { it.phoneNumber.toOptional() }
        .take(1)
        .replay()
        .refCount()

    val validEntry = saveClicks
        .withLatestFrom(entryChanges, savedNumbers)
        .filter { (_, entry, savedNumber) ->
          val validationErrors = entry.validate(savedNumber.toNullable(), numberValidator, dobValidator)
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
          // When prefilling fields, setting a blank phone number causes the text changed event on
          // the phone number field to get triggered with a blank string. Even though the previously
          // saved phone number is null, this gets saved then as a blank phone number. The next time
          // we edit this patient, the user is forced to enter a phone number since a blank phone
          // number already exists and it gets checked in the validations. This is a workaround to
          // prevent this situation by not creating a blank phone number in the first place.
          if (entry.phoneNumber.isBlank()) {
            Single.just(true)

          } else {
            patientRepository
                .createPhoneNumberForPatient(patientUuid, entry.phoneNumber, phoneNumberType = PatientPhoneNumberType.Mobile, active = true)
                .toSingleDefault(true)
          }
        }

    val saveOrUpdatePhoneNumber = Observable.merge(createNewPhoneNumber, updateExistingPhoneNumber)

    val savedPatient = screenCreatedStream
        .map { it.patient }
        .take(1)
        .replay()
        .refCount()

    val savedPatientAddress = screenCreatedStream
        .map { it.address }
        .replay()
        .refCount()

    val savePatientDetails = validEntry
        .withLatestFrom(savedPatient, savedPatientAddress)
        .map { (ongoingEditPatientEntry, patient, patientAddress) ->
          val updatedPatient = patientWithEdits(patient, ongoingEditPatientEntry)

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

  private fun patientWithEdits(
      patient: Patient,
      ongoingEditPatientEntry: OngoingEditPatientEntry
  ): Patient {
    return when (ongoingEditPatientEntry.ageOrDateOfBirth) {
      is EntryWithAge -> {
        patient.copy(
            fullName = ongoingEditPatientEntry.name,
            gender = ongoingEditPatientEntry.gender,
            dateOfBirth = null,
            age = coerceAgeFrom(patient.age, ongoingEditPatientEntry.ageOrDateOfBirth.age)
        )
      }
      is EntryWithDateOfBirth -> {
        patient.copy(
            fullName = ongoingEditPatientEntry.name,
            gender = ongoingEditPatientEntry.gender,
            age = null,
            dateOfBirth = LocalDate.parse(ongoingEditPatientEntry.ageOrDateOfBirth.dateOfBirth, dateOfBirthFormatter)
        )
      }
    }
  }

  private fun coerceAgeFrom(alreadySavedAge: Age?, enteredAge: String): Age {
    val enteredAgeValue = enteredAge.toInt()

    return when {
      // When prefilling the details, the age text changed event will get triggered, which will
      // trigger an update of the age and the age updated at timestamp which will change the age
      // calculations again. This handles that case.
      alreadySavedAge != null && alreadySavedAge.value == enteredAgeValue -> alreadySavedAge
      else -> Age(enteredAgeValue, Instant.now(utcClock))
    }
  }

  private fun toggleDatePatternInDateOfBirthLabel(events: Observable<UiEvent>): Observable<UiChange> {
    val dateFocusChanges = events
        .ofType<DateOfBirthFocusChanged>()
        .map { it.hasFocus }

    val dateTextAvailabilities = events
        .ofType<DateOfBirthChanged>()
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
        .ofType<DateOfBirthChanged>()
        .map { it.dateOfBirth.isBlank() }

    val isAgeBlanks = events
        .ofType<AgeChanged>()
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

  private fun closeScreenWithoutSaving(events: Observable<UiEvent>): Observable<UiChange> {
    val savedOngoingEntry = events
        .mapType<EditPatientScreenCreated, OngoingEditPatientEntry> { OngoingEditPatientEntry.from(it, dateOfBirthFormatter) }

    val ongoingEntryChanges = events
        .ofType<OngoingEditPatientEntryChanged>()
        .map { it.ongoingEditPatientEntry }

    val hasEntryChangedStream = events
        .ofType<PatientEditBackClicked>()
        .withLatestFrom(ongoingEntryChanges) { _, entry -> entry }
        .withLatestFrom(savedOngoingEntry)
        .map { (ongoingEntry, savedOngoingEntry) -> ongoingEntry != savedOngoingEntry }

    val confirmDiscardChanges = hasEntryChangedStream
        .filter { hasEntryChanged -> hasEntryChanged }
        .map { { ui: Ui -> ui.showDiscardChangesAlert() } }

    val closeScreenWithoutConfirmation = hasEntryChangedStream
        .filter { hasEntryChanged -> hasEntryChanged.not() }
        .map { { ui: Ui -> ui.goBack() } }

    return confirmDiscardChanges.mergeWith(closeScreenWithoutConfirmation)
  }
}
