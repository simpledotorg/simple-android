package org.resolvetosavelives.red.newentry.search

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.di.AppScope
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
                updatedAt = Instant.now(),
                syncPending = true)
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
                syncPending = true)
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

  private fun savePatient(patient: Patient): Completable {
    return Completable.fromAction {
      database.patientDao().save(patient)
    }
  }
}
