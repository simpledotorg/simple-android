package org.simple.clinic.facility

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import javax.inject.Inject

@AppScope
class FacilityRepository @Inject constructor(
    private val dao: Facility.RoomDao
) {

  // TODO: Maybe change the return type to Single if this function can never emit more than one facilities?
  fun currentFacility(userSession: UserSession): Observable<Facility> {
    return userSession.loggedInUser()
        .map {
          when (it) {
            is Just -> it.value.facilityUuid
            else -> throw AssertionError("User isn't logged in yet")
          }
        }
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
