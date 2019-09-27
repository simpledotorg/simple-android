package org.simple.clinic.editpatient

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.DateOfBirth.Type.EXACT
import org.simple.clinic.patient.DateOfBirth.Type.FROM_AGE
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

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
        .addConsumer(PrefillFormEffect::class.java) { (patient, address, phoneNumber) ->
          prefillFormFields(ui, patient, address, phoneNumber, userClock)
        }
        .addConsumer(ShowValidationErrorsEffect::class.java) {
          ui.showValidationErrors(it.validationErrors)
          ui.scrollToFirstFieldWithError()
        }
        .addConsumer(HideValidationErrorsEffect::class.java) { ui.hideValidationErrors(it.validationErrors) }
        .addAction(ShowDatePatternInDateOfBirthLabelEffect::class.java) { ui.showDatePatternInDateOfBirthLabel() }
        .addAction(HideDatePatternInDateOfBirthLabelEffect::class.java) { ui.hideDatePatternInDateOfBirthLabel() }
        .addAction(GoBackEffect::class.java) { ui.goBack() }
        .addAction(ShowDiscardChangesAlertEffect::class.java) { ui.showDiscardChangesAlert() }
        .addTransformer(SavePatientEffect::class.java) { savePatientEffects ->
          val sharedSavePatientEffects = savePatientEffects
              .share()

          val savePhoneNumbers = sharedSavePatientEffects
              .flatMapCompletable { (entry, patient, _, phoneNumber) ->
                if (phoneNumber == null && entry.phoneNumber.isBlank()) {
                  Completable.complete()
                } else if (phoneNumber != null && entry.phoneNumber.isNotBlank()) {
                  patientRepository
                      .updatePhoneNumberForPatient(patient.uuid, phoneNumber.copy(number = entry.phoneNumber))
                } else {
                  patientRepository
                      .createPhoneNumberForPatient(patient.uuid, entry.phoneNumber, phoneNumberType = PatientPhoneNumberType.Mobile, active = true)
                }
              }
              .toFlowable<EditPatientEvent>()
              .toObservable()

          val savePatients = sharedSavePatientEffects
              .map { (ongoingEditPatientEntry, patient, patientAddress, _) ->
                val updatedPatient = patientWithEdits(patient, ongoingEditPatientEntry, dateOfBirthFormatter, utcClock)

                val updatedAddress = patientAddress.copy(
                    colonyOrVillage = ongoingEditPatientEntry.colonyOrVillage,
                    state = ongoingEditPatientEntry.state,
                    district = ongoingEditPatientEntry.district
                )

                updatedPatient to updatedAddress
              }.flatMapSingle { (updatedPatient, updatedPatientAddress) ->
                patientRepository
                    .updatePatient(updatedPatient)
                    .andThen(patientRepository.updateAddressForPatient(updatedPatient.uuid, updatedPatientAddress))
                    .toSingleDefault(true)
              }
              .doOnNext { ui.goBack() }
              .flatMap { Observable.never<EditPatientEvent>() }

          savePhoneNumbers.mergeWith(savePatients)
        }
        .build()
  }

  private fun prefillFormFields(
      ui: EditPatientUi,
      patient: Patient,
      address: PatientAddress,
      phoneNumber: PatientPhoneNumber?,
      userClock: UserClock
  ) {
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
      EXACT -> ui.setPatientDateOfBirth(dateOfBirth.date)
      FROM_AGE -> ui.setPatientAge(dateOfBirth.estimateAge(userClock))
    }
  }

  private fun patientWithEdits(
      patient: Patient,
      ongoingEditPatientEntry: OngoingEditPatientEntry,
      dateOfBirthFormatter: DateTimeFormatter,
      utcClock: UtcClock
  ): Patient {
    return when (ongoingEditPatientEntry.ageOrDateOfBirth) {
      is OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithAge -> {
        patient.copy(
            fullName = ongoingEditPatientEntry.name,
            gender = ongoingEditPatientEntry.gender,
            dateOfBirth = null,
            age = coerceAgeFrom(patient.age, ongoingEditPatientEntry.ageOrDateOfBirth.age, utcClock)
        )
      }
      is OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth -> {
        patient.copy(
            fullName = ongoingEditPatientEntry.name,
            gender = ongoingEditPatientEntry.gender,
            age = null,
            dateOfBirth = LocalDate.parse(ongoingEditPatientEntry.ageOrDateOfBirth.dateOfBirth, dateOfBirthFormatter)
        )
      }
    }
  }

  private fun coerceAgeFrom(
      alreadySavedAge: Age?,
      enteredAge: String,
      utcClock: UtcClock
  ): Age {
    val enteredAgeValue = enteredAge.toInt()

    return when {
      // When prefilling the details, the age text changed event will get triggered, which will
      // trigger an update of the age and the age updated at timestamp which will change the age
      // calculations again. This handles that case.
      alreadySavedAge != null && alreadySavedAge.value == enteredAgeValue -> alreadySavedAge
      else -> Age(enteredAgeValue, Instant.now(utcClock))
    }
  }
}
