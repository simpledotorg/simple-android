package org.simple.clinic.facility

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.user.LoggedInUser
import org.simple.clinic.user.LoggedInUserFacilityMapping
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.None
import java.util.UUID
import javax.inject.Inject

@AppScope
class FacilityRepository @Inject constructor(
    private val facilityDao: Facility.RoomDao,
    private val userFacilityMappingDao: LoggedInUserFacilityMapping.RoomDao
) {

  fun facilities(): Observable<List<Facility>> {
    return facilityDao.facilities().toObservable()
  }

  fun associateUserWithFacilities(user: LoggedInUser, facilityIds: List<UUID>, currentFacility: UUID): Completable {
    if (facilityIds.contains(currentFacility).not()) {
      throw AssertionError()
    }
    return Completable.fromAction {
      userFacilityMappingDao.insert(user, facilityIds, currentFacility)
    }
  }

  fun currentFacility(userSession: UserSession): Observable<Facility> {
    return userSession.loggedInUser()
        .doOnNext {
          if (it === None) {
            throw AssertionError("User isn't logged in yet")
          }
        }
        .map { (user) -> user }
        .flatMap { userFacilityMappingDao.currentFacility(it.uuid).toObservable() }
  }

  fun facilityUuidsForUser(user: LoggedInUser): Observable<List<UUID>> {
    return userFacilityMappingDao
        .facilityUuidsFor(user.uuid)
        .toObservable()
  }

  fun mergeWithLocalData(payloads: List<FacilityPayload>): Completable {
    return payloads
        .toObservable()
        .filter { payload ->
          val localCopy = facilityDao.getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { it.toDatabaseModel(SyncStatus.DONE) }
        .toList()
        .flatMapCompletable { Completable.fromAction { facilityDao.save(it) } }
  }
}
