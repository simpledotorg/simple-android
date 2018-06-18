package org.simple.clinic.drugs

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.simple.clinic.drugs.sync.PrescribedDrugPayload
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class PrescriptionRepository @Inject constructor(
    private val dao: PrescribedDrug.RoomDao,
    private val facilityRepository: FacilityRepository
) {

  fun savePrescription(patientUuid: UUID, name: String, dosage: String?, rxNormCode: String?, isProtocolDrug: Boolean): Completable {
    val currentFacility = facilityRepository
        .currentFacility()
        .take(1)

    return currentFacility
        .flatMapCompletable { facility ->
          Completable.fromAction {
            val newMeasurement = PrescribedDrug(
                uuid = UUID.randomUUID(),
                name = name,
                dosage = dosage,
                rxNormCode = rxNormCode,
                isProtocolDrug = isProtocolDrug,
                isDeleted = false,
                syncStatus = SyncStatus.PENDING,
                patientUuid = patientUuid,
                facilityUuid = facility.uuid,
                createdAt = Instant.now(),
                updatedAt = Instant.now())
            dao.save(listOf(newMeasurement))
          }
        }
  }

  fun prescriptionsWithSyncStatus(status: SyncStatus): Single<List<PrescribedDrug>> {
    return dao
        .withSyncStatus(status)
        .firstOrError()
  }

  fun updatePrescriptionsSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus): Completable {
    return Completable.fromAction {
      dao.updateSyncStatus(oldStatus = oldStatus, newStatus = newStatus)
    }
  }

  fun updatePrescriptionsSyncStatus(prescriptionUuids: List<UUID>, newStatus: SyncStatus): Completable {
    if (prescriptionUuids.isEmpty()) {
      throw AssertionError()
    }
    return Completable.fromAction {
      dao.updateSyncStatus(uuids = prescriptionUuids, newStatus = newStatus)
    }
  }

  fun mergeWithLocalData(serverPayloads: List<PrescribedDrugPayload>): Completable {
    return serverPayloads
        .toObservable()
        .filter { payload ->
          val localCopy = dao.getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { it.toDatabaseModel(SyncStatus.DONE) }
        .toList()
        .flatMapCompletable { Completable.fromAction { dao.save(it) } }
  }

  fun prescriptionCount(): Single<Int> {
    return dao.drugCount().firstOrError()
  }
}
