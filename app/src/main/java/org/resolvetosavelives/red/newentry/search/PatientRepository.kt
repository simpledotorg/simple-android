package org.resolvetosavelives.red.newentry.search

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.di.AppScope
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
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

  fun searchPatientsWithAddresses(query: String): Observable<List<PatientWithAddress>> {
    return database.patientWithAddressDao()
        .search(query)
        .toObservable()
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
    val addressUuid = UUID.randomUUID().toString()

    val addressSave = ongoingEntry()
        .map { validEntry ->
          with(validEntry) {
            PatientAddress(
                uuid = addressUuid,
                colonyOrVillage = address!!.colonyOrVillage,
                district = address.district,
                state = address.state,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                syncPending = true)
          }
        }
        .flatMapCompletable { address -> saveAddress(address) }

    val patientSave = ongoingEntry()
        .map { entry ->
          with(entry) {
            Patient(
                uuid = UUID.randomUUID().toString(),
                fullName = personalDetails!!.fullName,
                gender = personalDetails.gender!!,
                status = PatientStatus.ACTIVE,

                //todo: both, dateOfBirth and ageWhenCreated, should not be null at the same time
                //todo: ageWhenCreated should be an int
                dateOfBirth = dateConverter(personalDetails.dateOfBirth),
                ageWhenCreated = personalDetails.ageWhenCreated?.toInt(),

                addressUuid = addressUuid,
                phoneNumberUuid = null,

                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                syncPending = true)
          }
        }
        .flatMapCompletable { patient -> savePatient(patient) }

    return addressSave.andThen(patientSave)
  }

  private fun dateConverter(dateOfBirth: String): LocalDate? {
    return LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: null
  }

  private fun saveAddress(address: PatientAddress): Completable {
    return Completable.fromAction {
      database.addressDao().save(address)
    }
  }

  private fun savePatient(patient: Patient): Completable {
    return Completable.fromAction {
      database.patientDao().save(patient)
    }
  }
}
