package org.simple.clinic.drugs

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.drugs.sync.PrescribedDrugPayload
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.canBeOverriddenByServerCopy
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.user.UserSession
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class PrescriptionRepository @Inject constructor(
    private val database: AppDatabase,
    private val dao: PrescribedDrug.RoomDao,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession
) : SynceableRepository<PrescribedDrug, PrescribedDrugPayload> {

  fun savePrescription(patientUuid: UUID, drug: ProtocolDrug, dosage: String): Completable {
    if (drug.dosages.contains(dosage).not()) {
      throw AssertionError("$drug does not contain this selected dosage")
    }

    return savePrescription(patientUuid, drug.name, dosage, drug.rxNormCode, isProtocolDrug = true)
  }

  fun savePrescription(patientUuid: UUID, name: String, dosage: String?, rxNormCode: String?, isProtocolDrug: Boolean): Completable {
    if (dosage != null && dosage.isBlank()) {
      throw AssertionError("Dosage cannot be both blank and non-null")
    }

    val currentFacility = facilityRepository
        .currentFacility(userSession)
        .take(1)

    return currentFacility
        .map { facility ->
          PrescribedDrug(
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
        }
        .flatMapCompletable { save(listOf(it)) }
  }

  override fun save(records: List<PrescribedDrug>): Completable {
    return Completable.fromAction { dao.save(records) }
  }

  fun softDeletePrescription(prescriptionUuid: UUID): Completable {
    return Completable.fromAction {
      database.runInTransaction {
        dao.softDelete(prescriptionUuid, deleted = true, updatedAt = Instant.now())
        dao.updateSyncStatus(listOf(prescriptionUuid), SyncStatus.PENDING)
      }
    }
  }

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): Single<List<PrescribedDrug>> {
    return dao
        .withSyncStatus(syncStatus)
        .firstOrError()
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus): Completable {
    return Completable.fromAction {
      dao.updateSyncStatus(oldStatus = from, newStatus = to)
    }
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus): Completable {
    if (ids.isEmpty()) {
      throw AssertionError()
    }
    return Completable.fromAction {
      dao.updateSyncStatus(uuids = ids, newStatus = to)
    }
  }

  override fun mergeWithLocalData(payloads: List<PrescribedDrugPayload>): Completable {
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

  override fun recordCount(): Single<Int> {
    return dao.count().firstOrError()
  }

  fun newestPrescriptionsForPatient(patientUuid: UUID): Observable<List<PrescribedDrug>> {
    return dao
        .forPatient(patientUuid)
        .toObservable()
  }
}
