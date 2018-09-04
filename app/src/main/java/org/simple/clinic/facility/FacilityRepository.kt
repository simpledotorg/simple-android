package org.simple.clinic.facility

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.user.LoggedInUserFacilityMapping
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
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

  fun associateUserWithFacilities(user: User, facilityIds: List<UUID>, currentFacility: UUID): Completable {
    return Completable.fromAction {
      userFacilityMappingDao.insertOrUpdate(user, facilityIds, currentFacility)
    }
  }

  fun associateUserWithFacility(userSession: UserSession, facilityId: UUID): Completable {
    return requireUser(userSession)
        .take(1)
        .flatMapCompletable {
          Completable.fromAction {
            userFacilityMappingDao.insertOrUpdate(it, listOf(facilityId), newCurrentFacilityUuid = facilityId)
          }
        }
  }

  fun currentFacility(userSession: UserSession): Observable<Facility> {
    return requireUser(userSession)
        .flatMap { currentFacility(it) }
  }

  fun currentFacility(user: User): Observable<Facility> {
    return userFacilityMappingDao.currentFacility(user.uuid).toObservable()
  }

  fun facilityUuidsForUser(user: User): Observable<List<UUID>> {
    return userFacilityMappingDao
        .facilityUuids(user.uuid)
        .toObservable()
  }

  private fun requireUser(userSession: UserSession): Observable<User> {
    return userSession.loggedInUser()
        .map { (user) ->
          if (user == null) {
            throw AssertionError("User isn't logged in yet")
          }
          user
        }
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
