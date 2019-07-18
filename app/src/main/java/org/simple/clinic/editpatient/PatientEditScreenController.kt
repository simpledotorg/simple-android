package org.simple.clinic.editpatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.editpatient.PatientEditValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.editpatient.PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.editpatient.PatientEditValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.PatientEditValidationError.STATE_EMPTY
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.OngoingEditPatientEntry
import org.simple.clinic.patient.OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.patient.OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.None
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.estimateCurrentAge
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

typealias Ui = PatientEditScreen
typealias UiChange = (Ui) -> Unit

class PatientEditScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val numberValidator: PhoneNumberValidator,
    private val utcClock: UtcClock,
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
        .flatMap { patientRepository.phoneNumber(it.uuid) }
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
            val estimatedAge = estimateCurrentAge(it.age!!.value, it.age.updatedAt, utcClock)
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

  private fun mergeWithOngoingEntryPatientEntryChanges(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
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

      val ageChanges = events
          .ofType<PatientEditAgeTextChanged>()
          .map { EntryWithAge(it.age) as OngoingEditPatientEntry.EitherAgeOrDateOfBirth }

      val dateOfBirthChanges = events
          .ofType<PatientEditDateOfBirthTextChanged>()
          .map { EntryWithDateOfBirth(it.dateOfBirth) as OngoingEditPatientEntry.EitherAgeOrDateOfBirth }

      val ageOrDateOfBirthChanges = Observable.merge(ageChanges, dateOfBirthChanges)

      val ongoingEntryChanges = Observables.combineLatest(
          nameChanges,
          genderChanges,
          colonyOrVillageChanges,
          districtChanges,
          stateChanges,
          phoneNumberChanges,
          ageOrDateOfBirthChanges
      ) { name, gender, colonyOrVillage, district, state, phoneNumber, ageOrDateOFBirth ->
        OngoingEditPatientEntryChanged(OngoingEditPatientEntry(
            name = name,
            gender = gender,
            phoneNumber = phoneNumber,
            colonyOrVillage = colonyOrVillage,
            district = district,
            state = state,
            ageOrDateOfBirth = ageOrDateOFBirth))
      }

      events.mergeWith(ongoingEntryChanges)
    }
  }

  private fun showValidationErrorsOnSaveClick(events: Observable<UiEvent>): Observable<UiChange> {
    val ongoingEditPatientEntryChanges = events.ofType<OngoingEditPatientEntryChanged>()

    val savedPhoneNumber = events.ofType<PatientEditScreenCreated>()
        .flatMap { patientRepository.phoneNumber(it.patientUuid) }
        .take(1)
        .replay()
        .refCount()

    return events.ofType<PatientEditSaveClicked>()
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
        PatientEditPhoneNumberTextChanged::class to setOf(PHONE_NUMBER_EMPTY, PHONE_NUMBER_LENGTH_TOO_LONG, PHONE_NUMBER_LENGTH_TOO_SHORT),
        PatientEditPatientNameTextChanged::class to setOf(FULL_NAME_EMPTY),
        PatientEditColonyOrVillageChanged::class to setOf(COLONY_OR_VILLAGE_EMPTY),
        PatientEditStateTextChanged::class to setOf(STATE_EMPTY),
        PatientEditDistrictTextChanged::class to setOf(DISTRICT_EMPTY),
        PatientEditAgeTextChanged::class to setOf(BOTH_DATEOFBIRTH_AND_AGE_ABSENT),
        PatientEditDateOfBirthTextChanged::class to setOf(INVALID_DATE_OF_BIRTH, DATE_OF_BIRTH_IN_FUTURE))

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
        .flatMap { patientRepository.phoneNumber(it) }
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
      alreadySavedAge != null && alreadySavedAge.value == enteredAgeValue -> {
        alreadySavedAge
      }
      else -> {
        Age(
            value = enteredAgeValue,
            updatedAt = Instant.now(utcClock),
            computedDateOfBirth = LocalDate.now(utcClock).minusYears(enteredAgeValue.toLong()))
      }
    }
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
            !dateBlank && ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE) }
            dateBlank && !ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(AGE_VISIBLE) }
            dateBlank && ageBlank -> { ui: Ui -> ui.setDateOfBirthAndAgeVisibility(BOTH_VISIBLE) }
            else -> throw AssertionError("Both date-of-birth and age cannot have user input at the same time")
          }
        }
  }

  private fun closeScreenWithoutSaving(events: Observable<UiEvent>): Observable<UiChange> {
    val ongoingEntryChanges = events
        .ofType<OngoingEditPatientEntryChanged>()
        .map { it.ongoingEditPatientEntry }

    val patientUuidStream = events.ofType<PatientEditScreenCreated>()
        .map { it.patientUuid }

    val savedPatient = patientUuidStream
        .flatMap { patientRepository.patient(it).take(1).unwrapJust() }
        .replay()
        .refCount()

    val savedPatientAddress = savedPatient
        .flatMap { patientRepository.address(it.addressUuid).take(1).unwrapJust() }
        .replay()
        .refCount()

    val savedNumbers = patientUuidStream
        .flatMap { patientRepository.phoneNumber(it).take(1) }
        .replay()
        .refCount()

    val hasEntryChangedStream = events
        .ofType<PatientEditBackClicked>()
        .withLatestFrom(ongoingEntryChanges) { _, entry -> entry }
        .withLatestFrom(savedPatient, savedPatientAddress, savedNumbers) { entry, patient, patientAddress, phoneNumbers ->
          hasPatientBeenEdited(
              entry = entry,
              savedPatient = patient,
              savedPatientAddress = patientAddress,
              savedPatientPhoneNumber = phoneNumbers.toNullable())
        }

    val confirmDiscardChanges = hasEntryChangedStream
        .filter { hasEntryChanged -> hasEntryChanged }
        .map { { ui: Ui -> ui.showDiscardChangesAlert() } }

    val closeScreenWithoutConfirmation = hasEntryChangedStream
        .filter { hasEntryChanged -> hasEntryChanged.not() }
        .map { { ui: Ui -> ui.goBack() } }

    return confirmDiscardChanges.mergeWith(closeScreenWithoutConfirmation)
  }

  private fun hasPatientBeenEdited(
      entry: OngoingEditPatientEntry,
      savedPatient: Patient,
      savedPatientAddress: PatientAddress,
      savedPatientPhoneNumber: PatientPhoneNumber?
  ): Boolean {
    val hasPatientChanged = entry.run {
      name != savedPatient.fullName || gender != savedPatient.gender
    }

    val hasPhoneNumberChanged = entry.run {
      when (savedPatientPhoneNumber) {
        null -> phoneNumber.isNotBlank()
        else -> savedPatientPhoneNumber.number != phoneNumber
      }
    }

    val hasDistrictOrStateChanged = entry.run {
      district != savedPatientAddress.district || state != savedPatientAddress.state
    }

    val hasColonyOrVillageChanged = entry.run {
      when (savedPatientAddress.colonyOrVillage) {
        null -> colonyOrVillage.isNotBlank()
        else -> savedPatientAddress.colonyOrVillage != colonyOrVillage
      }
    }

    val hasAgeOrDateOfBirthChanged = entry.run {
      when (ageOrDateOfBirth) {
        is EntryWithAge -> {
          val savedAgeAsString = savedPatient.age?.value?.toString()
          if (savedAgeAsString == null && ageOrDateOfBirth.age.isBlank()) {
            false

          } else {
            ageOrDateOfBirth.age != savedAgeAsString
          }
        }

        is EntryWithDateOfBirth -> {
          val savedDateOfBirthAsString = savedPatient.dateOfBirth?.format(dateOfBirthFormatter)

          if (savedDateOfBirthAsString == null && ageOrDateOfBirth.dateOfBirth.isBlank()) {
            false

          } else {
            ageOrDateOfBirth.dateOfBirth != savedDateOfBirthAsString
          }
        }
      }
    }

    return hasPatientChanged || hasPhoneNumberChanged || hasColonyOrVillageChanged || hasDistrictOrStateChanged || hasAgeOrDateOfBirthChanged
  }
}
