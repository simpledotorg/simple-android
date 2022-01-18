package org.simple.clinic.editpatient

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import org.simple.clinic.appconfig.Country
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PhoneNumberDetails
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Named

class EditPatientEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val utcClock: UtcClock,
    private val schedulersProvider: SchedulersProvider,
    private val country: Country,
    private val uuidGenerator: UuidGenerator,
    private val currentUser: Lazy<User>,
    private val inputFieldsFactory: InputFieldsFactory,
    @Named("date_for_user_input") private val dateOfBirthFormatter: DateTimeFormatter,
    @Assisted private val viewEffectsConsumer: Consumer<EditPatientViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<EditPatientViewEffect>
    ): EditPatientEffectHandler
  }

  fun build(): ObservableTransformer<EditPatientEffect, EditPatientEvent> {
    return RxMobius
        .subtypeEffectHandler<EditPatientEffect, EditPatientEvent>()
        .addTransformer(FetchBpPassportsEffect::class.java, fetchBpPassports(schedulersProvider.io()))
        .addTransformer(SavePatientEffect::class.java, savePatientTransformer(schedulersProvider.io()))
        .addTransformer(LoadInputFields::class.java, loadInputFields())
        .addTransformer(FetchColonyOrVillagesEffect::class.java, fetchColonyOrVillages())
        .addConsumer(EditPatientViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun fetchColonyOrVillages(): ObservableTransformer<FetchColonyOrVillagesEffect, EditPatientEvent> {
    return ObservableTransformer { fetchColonyOrVillagesEffect ->
      fetchColonyOrVillagesEffect
          .map { patientRepository.allColoniesOrVillagesInPatientAddress() }
          .map(::ColonyOrVillagesFetched)
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
          handleAlternativeId(sharedSavePatientEffects),
          linkBpPassportToThePatient(sharedSavePatientEffects)
      )
    }
  }

  private fun linkBpPassportToThePatient(
      savePatientEffects: Observable<SavePatientEffect>
  ): Observable<EditPatientEvent> {
    return savePatientEffects
        .filter(::isBpPassportAdded)
        .flatMapCompletable { savePatientEffect ->
          patientRepository
              .addIdentifierToPatient(
                  uuid = uuidGenerator.v4(),
                  assigningUser = currentUser.get(),
                  patientUuid = savePatientEffect.ongoingEntry.patientUuid,
                  identifier = Identifier(
                      value = savePatientEffect.ongoingEntry.bpPassports!!.last().value,
                      type = BpPassport
                  )
              )
              .ignoreElement()
        }
        .toObservable()
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
    val ageDetails = when (ongoingEntry.ageOrDateOfBirth) {
      is EntryWithAge -> coerceAgeFrom(patient.ageDetails, ongoingEntry.ageOrDateOfBirth.age)
      is EntryWithDateOfBirth -> readAgeDetailsFromEnteredDateOfBirth(ongoingEntry.ageOrDateOfBirth, patient)
    }

    return patient
        .withNameAndGender(ongoingEntry.name, ongoingEntry.gender)
        .withAgeDetails(ageDetails)
  }

  private fun coerceAgeFrom(
      recordedAgeDetails: PatientAgeDetails,
      enteredAge: String
  ): PatientAgeDetails {
    val enteredAgeValue = enteredAge.toInt()
    return when {
      recordedAgeDetails.doesRecordedAgeMatch(enteredAgeValue) -> recordedAgeDetails
      else -> recordedAgeDetails.withUpdatedAge(enteredAgeValue, utcClock)
    }
  }

  private fun readAgeDetailsFromEnteredDateOfBirth(
      ageOrDateOfBirth: EntryWithDateOfBirth,
      patient: Patient
  ): PatientAgeDetails {
    val dateOfBirth = LocalDate.parse(ageOrDateOfBirth.dateOfBirth, dateOfBirthFormatter)
    return patient.ageDetails.withDateOfBirth(dateOfBirth)
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
    fun isPhoneNumberPresent(
        existingPhoneNumber: PatientPhoneNumber?,
        enteredPhoneNumber: String
    ): Boolean =
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
    fun hasExistingPhoneNumber(
        existingPhoneNumber: PatientPhoneNumber?,
        enteredPhoneNumber: String
    ): Boolean =
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
    fun noExistingPhoneNumberButHasEnteredPhoneNumber(
        existingPhoneNumber: PatientPhoneNumber?,
        enteredPhoneNumber: String
    ): Boolean =
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

  private fun isBpPassportAdded(it: SavePatientEffect) = it.ongoingEntry.bpPassports?.isNotEmpty() == true

  private fun loadInputFields(): ObservableTransformer<LoadInputFields, EditPatientEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { inputFieldsFactory.provideFields() }
          .map(::InputFields)
          .map(::InputFieldsLoaded)
    }
  }
}
