package org.simple.clinic.editpatient

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import org.simple.clinic.appconfig.Country
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.DateOfBirth.Type.EXACT
import org.simple.clinic.patient.DateOfBirth.Type.FROM_AGE
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PhoneNumberDetails
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.user.User
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Named

class EditPatientEffectHandler @AssistedInject constructor(
    private val userClock: UserClock,
    private val patientRepository: PatientRepository,
    private val utcClock: UtcClock,
    private val schedulersProvider: SchedulersProvider,
    private val country: Country,
    private val uuidGenerator: UuidGenerator,
    private val currentUser: Lazy<User>,
    private val inputFieldsFactory: InputFieldsFactory,
    @Named("date_for_user_input") private val dateOfBirthFormatter: DateTimeFormatter,
    @Assisted private val ui: EditPatientUi
) {

  @AssistedFactory
  interface Factory {
    fun create(ui: EditPatientUi): EditPatientEffectHandler
  }

  fun build(): ObservableTransformer<EditPatientEffect, EditPatientEvent> {
    return RxMobius
        .subtypeEffectHandler<EditPatientEffect, EditPatientEvent>()
        .addConsumer(PrefillFormEffect::class.java, ::prefillFormFields, schedulersProvider.ui())
        .addConsumer(DisplayBpPassportsEffect::class.java, { displayBpPassports(it.bpPassports) }, schedulersProvider.ui())
        .addConsumer(ShowValidationErrorsEffect::class.java, ::showValidationErrors, schedulersProvider.ui())
        .addConsumer(HideValidationErrorsEffect::class.java, { ui.hideValidationErrors(it.validationErrors) }, schedulersProvider.ui())
        .addAction(ShowDatePatternInDateOfBirthLabelEffect::class.java, ui::showDatePatternInDateOfBirthLabel, schedulersProvider.ui())
        .addAction(HideDatePatternInDateOfBirthLabelEffect::class.java, ui::hideDatePatternInDateOfBirthLabel, schedulersProvider.ui())
        .addAction(GoBackEffect::class.java, ui::goBack, schedulersProvider.ui())
        .addAction(ShowDiscardChangesAlertEffect::class.java, ui::showDiscardChangesAlert, schedulersProvider.ui())
        .addTransformer(FetchBpPassportsEffect::class.java, fetchBpPassports(schedulersProvider.io()))
        .addTransformer(SavePatientEffect::class.java, savePatientTransformer(schedulersProvider.io()))
        .addTransformer(LoadInputFields::class.java, loadInputFields())
        .addConsumer(SetupUi::class.java, { ui.setupUi(it.inputFields) }, schedulersProvider.ui())
        .addTransformer(FetchColonyOrVillagesEffect::class.java, fetchColonyOrVillages())
        .build()
  }

  private fun fetchColonyOrVillages(): ObservableTransformer<FetchColonyOrVillagesEffect, EditPatientEvent> {
    return ObservableTransformer { fetchColonyOrVillagesEffect ->
      fetchColonyOrVillagesEffect
          .map { patientRepository.allColoniesOrVillagesInPatientAddress() }
          .map(::ColonyOrVillagesFetched)
    }
  }


  private fun displayBpPassports(bpPassports: List<BusinessId>) {
    val identifiers = bpPassports.map { it.identifier.displayValue() }
    ui.displayBpPassports(identifiers)
  }

  private fun prefillFormFields(prefillFormFieldsEffect: PrefillFormEffect) {
    val (patient, address, phoneNumber, alternateId) = prefillFormFieldsEffect

    with(ui) {
      setPatientName(patient.fullName)
      setGender(patient.gender)
      setState(address.state)
      setDistrict(address.district)
      setStreetAddress(address.streetAddress)
      setZone(address.zone)

      if (address.colonyOrVillage.isNullOrBlank().not()) {
        setColonyOrVillage(address.colonyOrVillage!!)
      }

      if (phoneNumber != null) {
        setPatientPhoneNumber(phoneNumber.number)
      }

      if (alternateId != null) {
        setAlternateId(alternateId.identifier.value)
      }
    }

    val dateOfBirth = DateOfBirth.fromPatient(patient, userClock)
    when (dateOfBirth.type) {
      EXACT -> ui.setPatientDateOfBirth(dateOfBirth.date)
      FROM_AGE -> ui.setPatientAge(dateOfBirth.estimateAge(userClock))
    }
  }

  private fun showValidationErrors(effect: ShowValidationErrorsEffect) {
    with(ui) {
      showValidationErrors(effect.validationErrors)
      scrollToFirstFieldWithError()
    }
  }

  private fun fetchBpPassports(scheduler: Scheduler): ObservableTransformer<FetchBpPassportsEffect, EditPatientEvent> {
    return ObservableTransformer { fetchBpPassportsEffect ->
      fetchBpPassportsEffect
          .flatMap { patientRepository.patientProfile(it.patientUuid).subscribeOn(scheduler) }
          .filterAndUnwrapJust()
          .map { patientProfile -> bpPassports(patientProfile) }
          .map(::BpPassportsFetched)
    }
  }

  private fun bpPassports(patientProfile: PatientProfile): List<BusinessId> {
    return patientProfile
        .withoutDeletedBusinessIds()
        .businessIds
        .filter { it.identifier.type == BpPassport }
  }

  private fun savePatientTransformer(scheduler: Scheduler): ObservableTransformer<SavePatientEffect, EditPatientEvent> {
    return ObservableTransformer { savePatientEffects ->
      val sharedSavePatientEffects = savePatientEffects
          .subscribeOn(scheduler)
          .share()

      Observable.merge(
          createOrUpdatePhoneNumber(sharedSavePatientEffects),
          savePatient(sharedSavePatientEffects),
          handleAlternativeId(sharedSavePatientEffects)
      )
    }
  }

  private fun savePatient(savePatientEffects: Observable<SavePatientEffect>): Observable<EditPatientEvent> {
    return savePatientEffects
        .map { (ongoingEditPatientEntry, patient, patientAddress, _) ->
          getUpdatedPatientAndAddress(patient, patientAddress, ongoingEditPatientEntry)
        }.flatMapSingle { (updatedPatient, updatedAddress) ->
          savePatientAndAddress(updatedPatient, updatedAddress)
        }
        .map { PatientSaved }
  }

  private fun getUpdatedPatientAndAddress(
      patient: Patient,
      patientAddress: PatientAddress,
      ongoingEntry: EditablePatientEntry
  ): Pair<Patient, PatientAddress> {
    val updatedPatient = updatePatient(patient, ongoingEntry)
    val updatedAddress = updateAddress(patientAddress, ongoingEntry)
    return updatedPatient to updatedAddress
  }

  private fun updatePatient(
      patient: Patient,
      ongoingEntry: EditablePatientEntry
  ): Patient {
    val patientWithoutAgeOrDateOfBirth = patient
        .withNameAndGender(ongoingEntry.name, ongoingEntry.gender)
        .withoutAgeAndDateOfBirth()

    return when (ongoingEntry.ageOrDateOfBirth) {
      is EntryWithAge -> {
        val age = coerceAgeFrom(patient.age, ongoingEntry.ageOrDateOfBirth.age)
        patientWithoutAgeOrDateOfBirth.withAge(age)
      }

      is EntryWithDateOfBirth -> {
        val dateOfBirth = LocalDate.parse(ongoingEntry.ageOrDateOfBirth.dateOfBirth, dateOfBirthFormatter)
        patientWithoutAgeOrDateOfBirth.withDateOfBirth(dateOfBirth)
      }
    }
  }

  private fun coerceAgeFrom(alreadySavedAge: Age?, enteredAge: String): Age {
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
    patientAddress.withLocality(
        colonyOrVillage = colonyOrVillage,
        district = district,
        state = state,
        zone = zone,
        streetAddress = streetAddress
    )
  }

  private fun createOrUpdatePhoneNumber(savePatientEffects: Observable<SavePatientEffect>): Observable<EditPatientEvent> {
    fun isPhoneNumberPresent(existingPhoneNumber: PatientPhoneNumber?, enteredPhoneNumber: String): Boolean =
        existingPhoneNumber != null || enteredPhoneNumber.isNotBlank()

    val effectsWithPhoneNumber = savePatientEffects
        .map { (entry, patient, _, phoneNumber) -> Triple(patient.uuid, phoneNumber, entry.phoneNumber) }
        .filter { (_, existingPhoneNumber, enteredPhoneNumber) ->
          isPhoneNumberPresent(existingPhoneNumber, enteredPhoneNumber)
        }
        .share()

    return Observable.merge(
        createPhoneNumber(effectsWithPhoneNumber),
        updatePhoneNumber(effectsWithPhoneNumber)
    )
  }

  private fun handleAlternativeId(
      savePatientEffects: Observable<SavePatientEffect>
  ): Observable<EditPatientEvent> {
    val createAlternativeIdStream = Maybe.fromCallable {
      country.alternativeIdentifierType
    }.flatMapObservable { identifierType ->
      createAlternativeId(savePatientEffects, identifierType)
    }

    return Observable.merge(
        createAlternativeIdStream,
        updateAlternativeId(savePatientEffects),
        deleteAlternativeId(savePatientEffects)
    )
  }

  private fun createAlternativeId(
      savePatientEffects: Observable<SavePatientEffect>,
      alternativeIdentifierType: IdentifierType
  ): Observable<EditPatientEvent> {
    return savePatientEffects
        .filter(::isAlternativeIdAdded)
        .flatMapCompletable { savePatientEffect ->
          patientRepository
              .addIdentifierToPatient(
                  uuid = uuidGenerator.v4(),
                  assigningUser = currentUser.get(),
                  patientUuid = savePatientEffect.ongoingEntry.patientUuid,
                  identifier = Identifier(
                      value = savePatientEffect.ongoingEntry.alternativeId,
                      type = alternativeIdentifierType
                  )
              )
              .ignoreElement()
        }
        .toObservable()
  }

  private fun updateAlternativeId(savePatientEffects: Observable<SavePatientEffect>): Observable<EditPatientEvent> {
    return savePatientEffects
        .filter(::isAlternativeIdModified)
        .map { it.saveAlternativeId?.updateIdentifierValue(it.ongoingEntry.alternativeId) }
        .flatMapCompletable { patientRepository.saveBusinessId(it) }
        .toObservable()
  }

  private fun deleteAlternativeId(savePatientEffects: Observable<SavePatientEffect>): Observable<EditPatientEvent> {
    return savePatientEffects
        .filter(::isAlternativeIdCleared)
        .map { it.saveAlternativeId }
        .flatMapCompletable(patientRepository::deleteBusinessId)
        .toObservable<EditPatientEvent>()
  }

  private fun updatePhoneNumber(
      phoneNumbers: Observable<Triple<UUID, PatientPhoneNumber?, String>>
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
      phoneNumbers: Observable<Triple<UUID, PatientPhoneNumber?, String>>
  ): Observable<EditPatientEvent> {
    fun noExistingPhoneNumberButHasEnteredPhoneNumber(existingPhoneNumber: PatientPhoneNumber?, enteredPhoneNumber: String): Boolean =
        existingPhoneNumber == null && enteredPhoneNumber.isNotBlank()

    return phoneNumbers
        .filter { (_, existingPhoneNumber, enteredPhoneNumber) ->
          noExistingPhoneNumberButHasEnteredPhoneNumber(existingPhoneNumber, enteredPhoneNumber)
        }
        .flatMapCompletable { (patientUuid, _, enteredPhoneNumber) ->
          patientRepository.createPhoneNumberForPatient(
              uuid = uuidGenerator.v4(),
              patientUuid = patientUuid,
              numberDetails = PhoneNumberDetails.mobile(enteredPhoneNumber),
              active = true
          )
        }.toObservable()
  }

  private fun savePatientAndAddress(
      updatedPatient: Patient,
      updatedAddress: PatientAddress
  ): Single<Boolean> {
    return patientRepository
        .updatePatient(updatedPatient)
        .andThen(patientRepository.updateAddressForPatient(updatedPatient.uuid, updatedAddress))
        // Doing this because Completables are not working properly
        .toSingleDefault(true)
  }

  private fun isAlternativeIdModified(it: SavePatientEffect) = it.saveAlternativeId != null && it.ongoingEntry.alternativeId.isNotBlank()

  private fun isAlternativeIdAdded(it: SavePatientEffect) = it.saveAlternativeId == null && it.ongoingEntry.alternativeId.isNotBlank()

  private fun isAlternativeIdCleared(it: SavePatientEffect) = it.saveAlternativeId != null && it.ongoingEntry.alternativeId.isBlank()

  private fun loadInputFields(): ObservableTransformer<LoadInputFields, EditPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { inputFieldsFactory.provideFields() }
          .map(::InputFields)
          .map(::InputFieldsLoaded)
    }
  }
}
