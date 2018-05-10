package org.resolvetosavelives.red.newentry.search

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class PatientRepository {

  private var patients: List<Patient> = ArrayList()
  private var ongoingPatientEntry: OngoingPatientEntry = OngoingPatientEntry()

  fun search(query: String?): Observable<List<Patient>> {
    if (query == null) {
      return Observable.just(patients)
    }

    return Observable.fromIterable(patients)
        .filter({ patient -> patient.fullName.contains(query, ignoreCase = true) })
        .toList()
        .toObservable()
  }

  fun save(patient: Patient): Completable {
    return Completable.fromAction({
      patients += patient
    })
  }

  fun ongoingEntry(): Single<OngoingPatientEntry> {
    return Single.just(ongoingPatientEntry)
  }

  fun save(ongoingEntry: OngoingPatientEntry): Completable {
    return Completable.fromAction({
      this.ongoingPatientEntry = ongoingEntry
    })
  }

  fun saveOngoingEntry(): Completable {
    return ongoingEntry()
        .map { entry -> entry.toPatient() }
        .flatMapCompletable { patient -> save(patient) }
  }
}
