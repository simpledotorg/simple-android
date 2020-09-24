package org.simple.clinic.drugs

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.drugs.sync.PrescribedDrugPayload
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.sync.SynceableRepository
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.util.UtcClock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@AppScope
class PrescriptionRepository @Inject constructor(
    private val database: AppDatabase,
    private val dao: PrescribedDrug.RoomDao,
    private val utcClock: UtcClock
) : SynceableRepository<PrescribedDrug, PrescribedDrugPayload> {

  fun savePrescription(
      uuid: UUID,
      patientUuid: UUID,
      drug: ProtocolDrug,
      facility: Facility
  ): Completable {
    return savePrescription(
        uuid = uuid,
        patientUuid = patientUuid,
        name = drug.name,
        dosage = drug.dosage,
        rxNormCode = drug.rxNormCode,
        isProtocolDrug = true,
        facility = facility
    )
  }

  fun savePrescription(
      uuid: UUID,
      patientUuid: UUID,
      name: String,
      dosage: String?,
      rxNormCode: String?,
      isProtocolDrug: Boolean,
      facility: Facility
  ): Completable {
    if (dosage != null && dosage.isBlank()) {
      throw AssertionError("Dosage cannot be both blank and non-null")
    }

    return Single.just(Instant.now(utcClock))
        .map { now ->
          PrescribedDrug(
              uuid = uuid,
              name = name,
              dosage = dosage,
              rxNormCode = rxNormCode,
              isDeleted = false,
              isProtocolDrug = isProtocolDrug,
              patientUuid = patientUuid,
              facilityUuid = facility.uuid,
              syncStatus = SyncStatus.PENDING,
              timestamps = Timestamps(now, now, null),
              frequency = null,
              durationInDays = null,
              teleconsultationId = null
          )
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

  override fun recordsWithSyncStatus(syncStatus: SyncStatus): List<PrescribedDrug> {
    return dao.withSyncStatus(syncStatus)
  }

  override fun setSyncStatus(from: SyncStatus, to: SyncStatus) {
    dao.updateSyncStatus(oldStatus = from, newStatus = to)
  }

  override fun setSyncStatus(ids: List<UUID>, to: SyncStatus) {
    if (ids.isEmpty()) {
      throw AssertionError()
    }

    dao.updateSyncStatus(uuids = ids, newStatus = to)
  }

  override fun mergeWithLocalData(payloads: List<PrescribedDrugPayload>) {
    val dirtyRecords = dao.recordIdsWithSyncStatus(SyncStatus.PENDING)

    val payloadsToSave = payloads
        .filterNot { it.uuid in dirtyRecords }
        .map { it.toDatabaseModel(SyncStatus.DONE) }

    dao.save(payloadsToSave)
  }

  override fun recordCount(): Observable<Int> {
    return dao.count().toObservable()
  }

  fun newestPrescriptionsForPatient(patientUuid: UUID): Observable<List<PrescribedDrug>> {
    return dao
        .forPatient(patientUuid)
        .toObservable()
  }

  fun newestPrescriptionsForPatientImmediate(patientUuid: UUID): List<PrescribedDrug> {
    return dao.forPatientImmediate(patientUuid)
  }

  @Deprecated("Use prescriptionImmediate() instead")
  fun prescription(prescriptionUuid: UUID): Observable<PrescribedDrug> = dao.prescription(prescriptionUuid).toObservable()

  fun prescriptionImmediate(prescriptionUuid: UUID): PrescribedDrug = dao.prescriptionImmediate(prescriptionUuid)!!

  override fun pendingSyncRecordCount(): Observable<Int> {
    return dao
        .count(SyncStatus.PENDING)
        .toObservable()
  }

  fun updateDrugDuration(id: UUID, duration: Duration) {
    dao.updateDrugDuration(
        id = id,
        durationInDays = duration.toDays().toInt(),
        updatedAt = Instant.now(utcClock),
        syncStatus = SyncStatus.PENDING
    )
  }

  fun updateDrugFrequency(id: UUID, drugFrequency: MedicineFrequency) {
    dao.updateDrugFrequenecy(
        id = id,
        drugFrequency = drugFrequency,
        updatedAt = Instant.now(utcClock),
        syncStatus = SyncStatus.PENDING
    )
  }

  fun addTeleconsultationIdToDrugs(prescribedDrugs: List<PrescribedDrug>, teleconsultationId: UUID) {
    dao.addTeleconsultationIdToDrugs(
        drugUuids = prescribedDrugs.map { it.uuid },
        teleconsultationId = teleconsultationId,
        updatedAt = Instant.now(utcClock),
        syncStatus = SyncStatus.PENDING
    )
  }
}
