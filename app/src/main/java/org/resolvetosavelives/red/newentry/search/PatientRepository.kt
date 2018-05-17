package org.resolvetosavelives.red.newentry.search

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.di.AppScope
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

  private fun save(patient: Patient): Completable {
    return Completable.fromAction({ database.patientDao().save(patient) })
  }

  // TODO: Add a test to ensure this method always returns a Single.
  // Changing this to Observable<> might destroy a lot of things.
  // We don't have tests for ensuring this behavior yet.
  fun ongoingEntry(): Single<OngoingPatientEntry> {
    return Single.just(ongoingPatientEntry)
  }

  fun save(ongoingEntry: OngoingPatientEntry): Completable {
    return Completable.fromAction({
      this.ongoingPatientEntry = ongoingEntry
    })
  }

  fun markOngoingEntryAsComplete(patientId: UUID): Completable {
    // TODO: Parse date and convert it to millis.
    val dateConverter: (String) -> Long = { formattedDate -> 0 }

    return ongoingEntry()
        .map { entry -> entry.toPatient(patientId, dateConverter) }
        .flatMapCompletable { patient -> save(patient) }
  }
}
