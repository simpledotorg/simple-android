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
    val userFacilityMappingDao: LoggedInUserFacilityMapping.RoomDao
) {

  fun facilities(): Observable<List<Facility>> {
    return facilityDao.facilities().toObservable()
  }

  fun associateUserWithFacilities(user: User, facilityIds: List<UUID>, currentFacility: UUID): Completable {
    return associateUserWithFacilities(user, facilityIds)
        .andThen(setCurrentFacility(user, currentFacility))
  }

  fun associateUserWithFacilities(user: User, facilityIds: List<UUID>): Completable {
    return Completable.fromAction { userFacilityMappingDao.insertOrUpdate(user, facilityIds) }
  }

  fun associateUserWithFacility(user: User, facility: Facility): Completable {
    return Completable.fromAction { userFacilityMappingDao.insertOrUpdate(user, listOf(facility.uuid)) }
  }

  fun setCurrentFacility(user: User, facility: Facility): Completable {
    return setCurrentFacility(user, facility.uuid)
  }

  private fun setCurrentFacility(user: User, facilityUuid: UUID): Completable {
    return Completable.fromAction { userFacilityMappingDao.changeCurrentFacility(user.uuid, facilityUuid) }
  }

  fun currentFacility(userSession: UserSession): Observable<Facility> {
    return userSession.requireLoggedInUser()
        .switchMap { currentFacility(it) }
  }

  fun currentFacility(user: User): Observable<Facility> {
    return userFacilityMappingDao.currentFacility(user.uuid).toObservable()
  }

  fun facilityUuidsForUser(user: User): Observable<List<UUID>> {
    return userFacilityMappingDao
        .facilityUuids(user.uuid)
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
