package org.resolvetosavelives.red.search

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.di.AppScope
import org.resolvetosavelives.red.search.SyncStatus.DONE
import org.resolvetosavelives.red.search.SyncStatus.PENDING
import org.resolvetosavelives.red.sync.patient.PatientPayload
import org.resolvetosavelives.red.util.LocalDateRoomTypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID
import javax.inject.Inject

@AppScope
class PatientRepository @Inject constructor(private val database: AppDatabase) {

  private var ongoingPatientEntry: OngoingPatientEntry = OngoingPatientEntry()

  @Deprecated(message = "Use searchPatientsWithAddressesAndPhoneNumbers() instead", replaceWith = ReplaceWith("searchPatientsWithAddressesAndPhoneNumbers(query)"))
  fun searchPatients(query: String): Observable<List<Patient>> {
    if (query.isEmpty()) {
      return database.patientDao()
          .allPatients()
          .toObservable()
    }

    return database.patientDao()
        .search(query)
        .toObservable()
  }

  fun patientCount(): Single<Int> {
    return database.patientDao()
        .patientCount()
        .firstOrError()
  }

  fun searchPatientsWithAddressesAndPhoneNumbers(query: String): Observable<List<PatientWithAddressAndPhone>> {
    if (query.isEmpty()) {
      return database.patientAddressPhoneDao()
          .allRecords()
          .toObservable()
    }

    return database.patientAddressPhoneDao()
        .search(query)
        .toObservable()
  }

  fun patientsWithSyncStatus(status: SyncStatus): Single<List<PatientWithAddressAndPhone>> {
    return database.patientAddressPhoneDao()
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

  fun mergeWithLocalData(serverPayloads: List<PatientPayload>): Completable {
    return serverPayloads
        .toObservable()
        .filter { payload ->
          val localCopy = database.patientDao().get(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .toList()
        .flatMapCompletable { payloads ->
          Completable.fromAction {
            val newOrUpdatedAddresses = payloads.map { it.address.toDatabaseModel() }
            database.addressDao().save(newOrUpdatedAddresses)

            val newOrUpdatedPatients = payloads.map { it.toDatabaseModel(DONE) }
            database.patientDao().save(newOrUpdatedPatients)
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

  fun saveOngoingEntryAsPatient(): Completable {
    val cachedOngoingEntry = ongoingEntry().cache()

    val ageValidation = cachedOngoingEntry
        .flatMapCompletable {
          // TODO: Should we also check that only age or date-of-birth should be present and not both?
          if (it.hasNullDateOfBirthAndAge()) {
            Completable.error(AssertionError("Both ageWhenCreated and dateOfBirth cannot be null."))
          } else {
            Completable.complete()
          }
        }

    val addressUuid = UUID.randomUUID()
    val phoneUuid = UUID.randomUUID()
    val patientUuid = UUID.randomUUID()

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

    val phoneNumbersSave = cachedOngoingEntry
        .flatMapCompletable { entry ->
          if (entry.phoneNumber == null) {
            Completable.complete()
          } else {
            val number = with(entry.phoneNumber) {
              PatientPhoneNumber(
                  uuid = phoneUuid,
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

    val patientSave = cachedOngoingEntry
        .map {
          with(it) {
            Patient(
                uuid = patientUuid,
                fullName = personalDetails!!.fullName,
                gender = personalDetails.gender!!,
                status = PatientStatus.ACTIVE,

                dateOfBirth = dateConverter(personalDetails.dateOfBirth),
                age = personalDetails.ageWhenCreated?.let {
                  Age(value = personalDetails.ageWhenCreated.toInt(), updatedAt = Instant.now())
                },

                addressUuid = addressUuid,

                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                syncStatus = PENDING)
          }
        }
        .flatMapCompletable { patient -> savePatient(patient) }

    return ageValidation
        .andThen(addressSave)
        .andThen(patientSave)
        .andThen(phoneNumbersSave)
  }

  private fun dateConverter(dateOfBirth: String?): LocalDate? {
    val converter = LocalDateRoomTypeConverter()
    return converter.toLocalDate(dateOfBirth)
  }

  private fun saveAddress(address: PatientAddress): Completable {
    return Completable.fromAction {
      database.addressDao().save(address)
    }
  }

  private fun savePhoneNumber(number: PatientPhoneNumber): Completable {
    return Completable.fromAction {
      database.phoneNumberDao().save(number)
    }
  }
}

