package org.simple.clinic.facility

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.user.UserSession
import javax.inject.Inject

@AppScope
class FacilityRepository @Inject constructor(
    private val dao: Facility.RoomDao
) {

  fun currentFacility(userSession: UserSession): Observable<Facility> {
    return userSession.loggedInUser()
        .take(1)
        .map { it.toNullable()!!.facilityUuid }
        .map { dao.getOne(it) }
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
