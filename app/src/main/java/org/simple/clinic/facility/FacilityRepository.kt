package org.simple.clinic.facility

import io.reactivex.Completable
import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toOptional
import java.util.UUID
import javax.inject.Inject

@AppScope
class FacilityRepository @Inject constructor(
    private val facilityDao: Facility.RoomDao,
    private val userDao: User.RoomDao
) : SynceableRepository<Facility, FacilityPayload> {

  fun facilities(searchQuery: String = ""): Observable<List<Facility>> {
    return if (searchQuery.isBlank()) {
      facilityDao.all().toObservable()
    } else {
      facilityDao.filteredByName(searchQuery).toObservable()
    }
  }

  fun facilitiesInCurrentGroup(searchQuery: String = ""): Observable<List<Facility>> {
    val filteredByName = {
      if (searchQuery.isBlank()) {
        facilityDao.all().toObservable()
      } else {
        facilityDao.filteredByName(searchQuery).toObservable()
      }
    }

    val filteredByNameAndGroup = { group: UUID ->
      if (searchQuery.isBlank()) {
        facilityDao.filteredByGroup(group).toObservable()
      } else {
        facilityDao.filteredByNameAndGroup(searchQuery, group).toObservable()
      }
    }

    return currentFacility()
        .switchMap { current ->
          when {
            current.groupUuid == null -> filteredByName()
            else -> filteredByNameAndGroup(current.groupUuid)
          }
        }
  }

  fun setCurrentFacilityImmediate(facility: Facility) {
    userDao.setCurrentFacility(facility.uuid)
  }

  fun setCurrentFacility(facilityUuid: UUID): Completable {
    return Completable.fromAction { userDao.setCurrentFacility(facilityUuid) }
  }

  fun currentFacility(): Observable<Facility> {
    return userDao.currentFacility().toObservable()
  }

  fun currentFacilityImmediate(): Facility? {
    return userDao.currentFacilityImmediate()
  }

  fun currentFacilityUuid(): UUID? {
    return userDao.currentFacilityUuid()
  }

  override fun mergeWithLocalData(payloads: List<FacilityPayload>) {
    val dirtyRecords = facilityDao.recordIdsWithSyncStatus(SyncStatus.PENDING)

    val payloadsToSave = payloads
        .filterNot { it.uuid in dirtyRecords }
        .map { it.toDatabaseModel(SyncStatus.DONE) }

    facilityDao.save(payloadsToSave)
  }

  override fun save(records: List<Facility>): Completable {
    return Completable.fromAction { facilityDao.save(records) }
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): List<Facility> {
    return facilityDao.withSyncStatus(syncStatus)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    facilityDao.updateSyncStatus(oldStatus = from, newStatus = to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    if (ids.isEmpty()) {
      throw AssertionError("IDs must not be empty!")
    }

    facilityDao.updateSyncStatus(uuids = ids, newStatus = to)
  }

  override fun recordCount(): Observable<Int> {
    return facilityDao.count().toObservable()
  }

  override fun pendingSyncRecordCount(): Observable<Int> {
    return facilityDao.count(SyncStatus.PENDING).toObservable()
  }

  fun facility(uuid: UUID): Optional<Facility> {
    return facilityDao.getOne(uuid).toOptional()
  }
}
