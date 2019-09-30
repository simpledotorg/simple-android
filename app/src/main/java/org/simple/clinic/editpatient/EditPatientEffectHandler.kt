package org.simple.clinic.editpatient

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.DateOfBirth.Type.EXACT
import org.simple.clinic.patient.DateOfBirth.Type.FROM_AGE
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType.Mobile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID

object EditPatientEffectHandler {
  fun createEffectHandler(
      ui: EditPatientUi,
      userClock: UserClock,
      patientRepository: PatientRepository,
      utcClock: UtcClock,
      dateOfBirthFormatter: DateTimeFormatter
  ): ObservableTransformer<EditPatientEffect, EditPatientEvent> {
    return RxMobius
        .subtypeEffectHandler<EditPatientEffect, EditPatientEvent>()
        .addConsumer(PrefillFormEffect::class.java) { prefillFormFields(it, ui, userClock) }
        .addConsumer(ShowValidationErrorsEffect::class.java) { showValidationErrors(it, ui) }
        .addConsumer(HideValidationErrorsEffect::class.java) { ui.hideValidationErrors(it.validationErrors) }
        .addAction(ShowDatePatternInDateOfBirthLabelEffect::class.java) { ui.showDatePatternInDateOfBirthLabel() }
        .addAction(HideDatePatternInDateOfBirthLabelEffect::class.java) { ui.hideDatePatternInDateOfBirthLabel() }
        .addAction(GoBackEffect::class.java) { ui.goBack() }
        .addAction(ShowDiscardChangesAlertEffect::class.java) { ui.showDiscardChangesAlert() }
        .addTransformer(SavePatientEffect::class.java, savePatientTransformer(patientRepository, utcClock, dateOfBirthFormatter))
        .build()
  }

  private fun prefillFormFields(
      prefillFormFieldsEffect: PrefillFormEffect,
      ui: EditPatientUi,
      userClock: UserClock
  ) {
    val (patient, address, phoneNumber) = prefillFormFieldsEffect

    with(ui) {
      setPatientName(patient.fullName)
      setGender(patient.gender)
      setState(address.state)
      setDistrict(address.district)

      if (address.colonyOrVillage.isNullOrBlank().not()) {
        setColonyOrVillage(address.colonyOrVillage!!)
      }

      if (phoneNumber != null) {
        setPatientPhoneNumber(phoneNumber.number)
      }
    }

    val dateOfBirth = DateOfBirth.fromPatient(patient, userClock)
    when (dateOfBirth.type) {
      EXACT -> ui.setPatientDateOfBirth(dateOfBirth.date)
      FROM_AGE -> ui.setPatientAge(dateOfBirth.estimateAge(userClock))
    }
  }

  private fun showValidationErrors(
      effect: ShowValidationErrorsEffect,
      ui: EditPatientUi
  ) {
    with(ui) {
      showValidationErrors(effect.validationErrors)
      scrollToFirstFieldWithError()
    }
  }

  private fun savePatientTransformer(
      patientRepository: PatientRepository,
      utcClock: UtcClock,
      dateOfBirthFormatter: DateTimeFormatter
  ): ObservableTransformer<SavePatientEffect, EditPatientEvent> {
    return ObservableTransformer { savePatientEffects ->
      val sharedSavePatientEffects = savePatientEffects
          .share()

      Observable.merge(
          createOrUpdatePhoneNumber(sharedSavePatientEffects, patientRepository),
          savePatient(sharedSavePatientEffects, patientRepository, utcClock, dateOfBirthFormatter)
      )
    }
  }

  private fun savePatient(
      savePatientEffects: Observable<SavePatientEffect>,
      patientRepository: PatientRepository,
      utcClock: UtcClock,
      dateOfBirthFormatter: DateTimeFormatter
  ): Observable<EditPatientEvent> {
    return savePatientEffects
        .map { (ongoingEditPatientEntry, patient, patientAddress, _) ->
          getUpdatedPatientAndAddress(patient, patientAddress, ongoingEditPatientEntry, utcClock, dateOfBirthFormatter)
        }.flatMapSingle { (updatedPatient, updatedAddress) ->
          savePatientAndAddress(patientRepository, updatedPatient, updatedAddress)
        }
        .map { PatientSaved }
  }

  private fun getUpdatedPatientAndAddress(
      patient: Patient,
      patientAddress: PatientAddress,
      ongoingEntry: EditablePatientEntry,
      utcClock: UtcClock,
      dateOfBirthFormatter: DateTimeFormatter
  ): Pair<Patient, PatientAddress> {
    val updatedPatient = updatePatient(patient, ongoingEntry, dateOfBirthFormatter, utcClock)
    val updatedAddress = updateAddress(patientAddress, ongoingEntry)
    return updatedPatient to updatedAddress
  }

  private fun updatePatient(
      patient: Patient,
      ongoingEntry: EditablePatientEntry,
      dateOfBirthFormatter: DateTimeFormatter,
      utcClock: UtcClock
  ): Patient {
    val patientWithoutAgeOrDateOfBirth = patient
        .withNameAndGender(ongoingEntry.name, ongoingEntry.gender)
        .withoutAgeAndDateOfBirth()

    return when (ongoingEntry.ageOrDateOfBirth) {
      is EntryWithAge -> {
        val age = coerceAgeFrom(patient.age, ongoingEntry.ageOrDateOfBirth.age, utcClock)
        patientWithoutAgeOrDateOfBirth.withAge(age)
      }

      is EntryWithDateOfBirth -> {
        val dateOfBirth = LocalDate.parse(ongoingEntry.ageOrDateOfBirth.dateOfBirth, dateOfBirthFormatter)
        patientWithoutAgeOrDateOfBirth.withDateOfBirth(dateOfBirth)
      }
    }
  }

  private fun coerceAgeFrom(alreadySavedAge: Age?, enteredAge: String, utcClock: UtcClock): Age {
    val enteredAgeValue = enteredAge.toInt()
    return when {
      alreadySavedAge != null && alreadySavedAge.value == enteredAgeValue -> alreadySavedAge
      else -> Age(enteredAgeValue, Instant.now(utcClock))
    }
  }

  private fun updateAddress(
      patientAddress: PatientAddress,
      ongoingEntry: EditablePatientEntry
  ): PatientAddress = with(ongoingEntry) {
    patientAddress.withLocality(colonyOrVillage, district, state)
  }

  private fun createOrUpdatePhoneNumber(
      savePatientEffects: Observable<SavePatientEffect>,
      patientRepository: PatientRepository
  ): Observable<EditPatientEvent> {
    fun isPhoneNumberPresent(existingPhoneNumber: PatientPhoneNumber?, enteredPhoneNumber: String): Boolean =
        existingPhoneNumber != null || enteredPhoneNumber.isNotBlank()

    val effectsWithPhoneNumber = savePatientEffects
        .map { (entry, patient, _, phoneNumber) -> Triple(patient.uuid, phoneNumber, entry.phoneNumber) }
        .filter { (_, existingPhoneNumber, enteredPhoneNumber) ->
          isPhoneNumberPresent(existingPhoneNumber, enteredPhoneNumber)
        }
        .share()

    return Observable.merge(
        createPhoneNumber(effectsWithPhoneNumber, patientRepository),
        updatePhoneNumber(effectsWithPhoneNumber, patientRepository)
    )
  }

  private fun updatePhoneNumber(
      phoneNumbers: Observable<Triple<UUID, PatientPhoneNumber?, String>>,
      patientRepository: PatientRepository
  ): Observable<EditPatientEvent> {
    fun hasExistingPhoneNumber(existingPhoneNumber: PatientPhoneNumber?, enteredPhoneNumber: String): Boolean =
        existingPhoneNumber != null && enteredPhoneNumber.isNotBlank()

    return phoneNumbers
        .filter { (_, existingPhoneNumber, enteredPhoneNumber) ->
          hasExistingPhoneNumber(existingPhoneNumber, enteredPhoneNumber)
        }
        .flatMapCompletable { (patientUuid, existingPhoneNumber, enteredPhoneNumber) ->
          requireNotNull(existingPhoneNumber)
          patientRepository.updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.withNumber(enteredPhoneNumber))
        }
        .toObservable()
  }

  private fun createPhoneNumber(
      phoneNumbers: Observable<Triple<UUID, PatientPhoneNumber?, String>>,
      patientRepository: PatientRepository
  ): Observable<EditPatientEvent> {
    fun noExistingPhoneNumberButHasEnteredPhoneNumber(existingPhoneNumber: PatientPhoneNumber?, enteredPhoneNumber: String): Boolean =
        existingPhoneNumber == null && enteredPhoneNumber.isNotBlank()

    return phoneNumbers
        .filter { (_, existingPhoneNumber, enteredPhoneNumber) ->
          noExistingPhoneNumberButHasEnteredPhoneNumber(existingPhoneNumber, enteredPhoneNumber)
        }
        .flatMapCompletable { (patientUuid, _, enteredPhoneNumber) ->
          patientRepository.createPhoneNumberForPatient(patientUuid, enteredPhoneNumber, Mobile, true)
        }.toObservable()
  }

  private fun savePatientAndAddress(
      patientRepository: PatientRepository,
      updatedPatient: Patient,
      updatedAddress: PatientAddress
  ): Single<Boolean> {
    return patientRepository
        .updatePatient(updatedPatient)
        .andThen(patientRepository.updateAddressForPatient(updatedPatient.uuid, updatedAddress))
        // Doing this because Completables are not working properly
        .toSingleDefault(true)
  }
}
