package org.resolvetosavelives.red.newentry.search

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.di.AppScope
import org.resolvetosavelives.red.newentry.search.SyncStatus.DONE
import org.resolvetosavelives.red.newentry.search.SyncStatus.INVALID
import org.resolvetosavelives.red.newentry.search.SyncStatus.IN_FLIGHT
import org.resolvetosavelives.red.newentry.search.SyncStatus.PENDING
import org.resolvetosavelives.red.sync.PatientPayload
import org.resolvetosavelives.red.util.LocalDateRoomTypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID
import javax.inject.Inject

@AppScope
class PatientRepository @Inject constructor(private val database: AppDatabase) {

  private var ongoingPatientEntry: OngoingPatientEntry = OngoingPatientEntry()

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

  fun patientCount(): Observable<Int> {
    return database.patientDao()
        .patientCount()
        .toObservable()
  }

  fun searchPatientsWithAddresses(query: String): Observable<List<PatientWithAddress>> {
    return database.patientWithAddressDao()
        .search(query)
        .toObservable()
  }

  fun patientsWithSyncStatus(status: SyncStatus): Single<List<PatientWithAddress>> {
    return database.patientWithAddressDao()
        .withSyncStatus(status)
        .firstOrError()
  }

  fun updatePatientsSyncStatus(fromStatus: SyncStatus, toStatus: SyncStatus): Completable {
    return Completable.fromAction {
      database.patientDao().updateSyncStatus(oldStatus = fromStatus, newStatus = toStatus)
    }
  }

  fun updatePatientsSyncStatus(patientUuids: List<String>, newStatus: SyncStatus): Completable {
    return Completable.fromAction {
      database.patientDao().updateSyncStatus(uuids = patientUuids, newStatus = newStatus)
    }
  }

  private fun savePatient(patient: Patient): Completable {
    return Completable.fromAction({ database.patientDao().save(patient) })
  }

  fun mergeWithLocalData(payloadsFromServer: List<PatientPayload>): Completable {
    return payloadsFromServer
        .toObservable()
        .filter { payload ->
          val localCopy = database.patientDao().get(payload.uuid)
          when (localCopy?.syncStatus) {
            PENDING -> false
            IN_FLIGHT -> false
            INVALID -> true
            DONE -> true
            null -> true
          }
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
    val ageValidation = ongoingEntry()
        .flatMapCompletable {
          if (it.hasNullDateOfBirthAndAge()) {
            Completable.error(AssertionError("Both ageWhenCreated and dateOfBirth cannot be null."))
          } else {
            Completable.complete()
          }
        }

    val addressUuid = UUID.randomUUID().toString()

    val addressSave = ongoingEntry()
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

    val patientSave = ongoingEntry()
        .map {
          with(it) {
            Patient(
                uuid = UUID.randomUUID().toString(),
                fullName = personalDetails!!.fullName,
                gender = personalDetails.gender!!,
                status = PatientStatus.ACTIVE,

                dateOfBirth = dateConverter(personalDetails.dateOfBirth),
                ageWhenCreated = personalDetails.ageWhenCreated?.toInt(),

                addressUuid = addressUuid,
                phoneNumberUuid = null,

                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                syncStatus = PENDING)
          }
        }
        .flatMapCompletable { patient -> savePatient(patient) }

    return ageValidation
        .andThen(addressSave)
        .andThen(patientSave)
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
}

