package org.resolvetosavelives.red.patient

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.di.AppScope
import org.resolvetosavelives.red.patient.OngoingPatientEntry.ValidationResult
import org.resolvetosavelives.red.patient.SyncStatus.DONE
import org.resolvetosavelives.red.patient.SyncStatus.PENDING
import org.resolvetosavelives.red.patient.sync.PatientPayload
import org.resolvetosavelives.red.util.Just
import org.resolvetosavelives.red.util.None
import org.resolvetosavelives.red.util.Optional
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@AppScope
class PatientRepository @Inject constructor(private val database: AppDatabase) {

  companion object {
    val dateOfTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy", Locale.ENGLISH)
  }

  private val ageFuzziness: Int = 3

  private var ongoingPatientEntry: OngoingPatientEntry = OngoingPatientEntry()

  fun searchPatientsAndPhoneNumbers(query: String?): Observable<List<PatientSearchResult>> {
    if (query.isNullOrEmpty()) {
      return database.patientSearchDao()
          .allRecords()
          .toObservable()
    }

    return database.patientSearchDao()
        .search(query!!)
        .toObservable()
  }

  fun searchPatientsAndPhoneNumbers(query: String?, assumedAge: Int): Observable<List<PatientSearchResult>> {
    val ageUpperBound = assumedAge + ageFuzziness
    val ageLowerBound = assumedAge - ageFuzziness

    val dateOfBirthUpperBound = LocalDate.now(ZoneOffset.UTC).minusYears(ageUpperBound.toLong()).toString()
    val dateOfBirthLowerBound = LocalDate.now(ZoneOffset.UTC).minusYears(ageLowerBound.toLong()).toString()

    if (query.isNullOrEmpty()) {
      return database.patientSearchDao()
          .allRecords()
          .toObservable()
    }

    return database.patientSearchDao()
        .search(query!!, dateOfBirthUpperBound, dateOfBirthLowerBound)
        .toObservable()
  }

  fun patientCount(): Single<Int> {
    return database.patientDao()
        .patientCount()
        .firstOrError()
  }

  fun patientsWithSyncStatus(status: SyncStatus): Single<List<PatientSearchResult>> {
    return database.patientSearchDao()
        .withSyncStatus(status)
        .firstOrError()
  }

  fun updatePatientsSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus): Completable {
    return Completable.fromAction {
      database.patientDao().updateSyncStatus(oldStatus = oldStatus, newStatus = newStatus)
    }
  }

  fun updatePatientsSyncStatus(patientUuids: List<UUID>, newStatus: SyncStatus): Completable {
    if (patientUuids.isEmpty()) {
      throw AssertionError()
    }
    return Completable.fromAction {
      database.patientDao().updateSyncStatus(uuids = patientUuids, newStatus = newStatus)
    }
  }

  private fun savePatient(patient: Patient): Completable {
    return Completable.fromAction({ database.patientDao().save(patient) })
  }

  fun patient(uuid: UUID): Observable<Optional<Patient>> {
    return database.patientDao()
        .patient(uuid)
        .toObservable()
        .map { patients ->
          when {
            patients.isNotEmpty() -> Just(patients.first())
            else -> None
          }
        }
  }

  fun mergeWithLocalData(serverPayloads: List<PatientPayload>): Completable {
    return serverPayloads
        .toObservable()
        .filter { payload ->
          val localCopy = database.patientDao().getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .toList()
        .flatMapCompletable { payloads ->
          Completable.fromAction {
            val newOrUpdatedAddresses = payloads.map { it.address.toDatabaseModel() }
            database.addressDao().save(newOrUpdatedAddresses)

            val newOrUpdatedPatients = payloads.map { it.toDatabaseModel(newStatus = DONE) }
            database.patientDao().save(newOrUpdatedPatients)

            val newOrUpdatedPhoneNumbers = payloads
                .filter { it.phoneNumbers != null }
                .flatMap { patientPayload ->
                  patientPayload.phoneNumbers!!.map { it.toDatabaseModel(patientPayload.uuid) }
                }
            if (newOrUpdatedPhoneNumbers.isNotEmpty()) {
              database.phoneNumberDao().save(newOrUpdatedPhoneNumbers)
            }
          }
        }
  }

  fun ongoingEntry(): Single<OngoingPatientEntry> {
    return Single.fromCallable({ ongoingPatientEntry })
  }

  fun saveOngoingEntry(ongoingEntry: OngoingPatientEntry): Completable {
    return Completable.fromAction({
      this.ongoingPatientEntry = ongoingEntry
    })
  }

  fun saveOngoingEntryAsPatient(): Single<Patient> {
    val cachedOngoingEntry = ongoingEntry().cache()

    val validation = cachedOngoingEntry
        .flatMapCompletable {
          val validationResult = it.validateForSaving()
          when (validationResult) {
            is ValidationResult.Valid -> Completable.complete()
            is ValidationResult.Invalid -> Completable.error(validationResult.error)
          }
        }

    val addressUuid = UUID.randomUUID()
    val addressSave = cachedOngoingEntry
        .map {
          with(it) {
            PatientAddress(
                uuid = addressUuid,
                colonyOrVillage = address!!.colonyOrVillage,
                district = address.district,
                state = address.state,
                createdAt = Instant.now(),
                updatedAt = Instant.now())
          }
        }
        .flatMapCompletable { address -> saveAddress(address) }

    val patientUuid = UUID.randomUUID()
    val sharedPatient = cachedOngoingEntry
        .map {
          with(it) {
            Patient(
                uuid = patientUuid,
                fullName = personalDetails!!.fullName,
                gender = personalDetails.gender!!,
                status = PatientStatus.ACTIVE,

                dateOfBirth = convertToDate(personalDetails.dateOfBirth),
                age = personalDetails.age?.let {
                  Age(value = personalDetails.age.toInt(),
                      updatedAt = Instant.now(),
                      computedDateOfBirth = LocalDate.now().minusYears(personalDetails.age.toLong()))
                },

                addressUuid = addressUuid,

                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                syncStatus = PENDING)
          }
        }
        .cache()

    val patientSave = sharedPatient
        .flatMapCompletable { patient -> savePatient(patient) }

    val phoneNumberSave = cachedOngoingEntry
        .flatMapCompletable { entry ->
          if (entry.phoneNumber == null) {
            Completable.complete()
          } else {
            val number = with(entry.phoneNumber) {
              PatientPhoneNumber(
                  uuid = UUID.randomUUID(),
                  patientUuid = patientUuid,
                  phoneType = type,
                  number = number,
                  active = active,
                  createdAt = Instant.now(),
                  updatedAt = Instant.now())
            }
            savePhoneNumber(number)
          }
        }

    return validation
        .andThen(addressSave)
        .andThen(patientSave)
        .andThen(phoneNumberSave)
        .andThen(sharedPatient)
  }

  private fun convertToDate(dateOfBirth: String?): LocalDate? {
    return dateOfBirth?.let {
      val formatter = dateOfTimeFormatter
      formatter.parse(dateOfBirth, LocalDate::from)
    }
  }

  private fun saveAddress(address: PatientAddress): Completable {
    return Completable.fromAction {
      database.addressDao().save(address)
    }
  }

  fun address(addressUuid: UUID): Observable<Optional<PatientAddress>> {
    return database.addressDao()
        .address(addressUuid)
        .toObservable()
        .map { addresses ->
          when {
            addresses.isNotEmpty() -> Just(addresses.first())
            else -> None
          }
        }
  }

  private fun savePhoneNumber(number: PatientPhoneNumber): Completable {
    return Completable.fromAction {
      database.phoneNumberDao().save(listOf(number))
    }
  }

  fun phoneNumbers(patientUuid: UUID): Observable<Optional<PatientPhoneNumber>> {
    return database.phoneNumberDao()
        .phoneNumber(patientUuid)
        .toObservable()
        .map { numbers ->
          when {
            numbers.isNotEmpty() -> Just(numbers.first())
            else -> None
          }
        }
  }
}

