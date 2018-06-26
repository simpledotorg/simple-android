package org.simple.clinic.facility

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class FacilityRepository @Inject constructor(
    private val dao: Facility.RoomDao
) {

  companion object {
    private val DUMMY_FACILITY = Facility(
        UUID.randomUUID(),
        "TODO",
        null,
        null,
        null,
        "Bathinda",
        "Punjab",
        "India",
        null,
        Instant.now(),
        Instant.now(),
        SyncStatus.PENDING)
  }

  fun currentFacility(): Observable<Facility> {
    return Observable.just(DUMMY_FACILITY)
  }

  fun mergeWithLocalData(payloads: List<FacilityPayload>): Completable {
    return payloads
        .toObservable()
        .filter { payload ->
          val localCopy = dao.getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { it.toDatabaseModel(SyncStatus.DONE) }
        .toList()
        .flatMapCompletable { Completable.fromAction { dao.save(it) } }
  }
}
