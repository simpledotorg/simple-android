package org.resolvetosavelives.red.newentry.search

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.di.AppScope
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID
import javax.inject.Inject

@AppScope
class PatientRepository @Inject constructor(private val database: AppDatabase) {

  private var ongoingPatientEntry: OngoingPatientEntry = OngoingPatientEntry()

  fun search(query: String): Observable<List<Patient>> {
    if (query.isEmpty()) {
      return database.patientDao()
          .allPatients()
          .toObservable()
    }

    return database.patientDao()
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
    // TODO: Parse date and convert it to millis.
    // todo: ensure either age, or DOB is not null.
    val dateConverter: (String) -> LocalDate? = { formattedDate -> null }

    val addressUuid = UUID.randomUUID().toString()

    val single = ongoingEntry()

    val addressSave = single
        .map({ entry ->
          with(entry) {
            PatientAddress(
                uuid = addressUuid,
                colonyOrVillage = address!!.colonyOrVillage,
                district = address.district,
                state = address.state,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                syncPending = true)
          }
        })
        .flatMapCompletable { address -> saveAddress(address) }

    val patientSave = single
        .map { entry ->
          with(entry) {
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
                syncPending = true)
          }
        }
        .flatMapCompletable { patient -> savePatient(patient) }

    return addressSave.andThen(patientSave)
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
