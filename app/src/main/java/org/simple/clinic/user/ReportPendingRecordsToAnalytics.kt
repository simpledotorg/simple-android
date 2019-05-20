package org.simple.clinic.user

import io.reactivex.Completable
import io.reactivex.rxkotlin.Singles
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.SyncStatus.PENDING
import org.threeten.bp.Instant
import javax.inject.Inject

class ReportPendingRecordsToAnalytics @Inject constructor(
    private val patientRepository: PatientRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val appointmentRepository: AppointmentRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository
) {
  fun report(): Completable {
    return Singles
        .zip(
            patientRepository.recordsWithSyncStatus(PENDING),
            bloodPressureRepository.recordsWithSyncStatus(PENDING),
            appointmentRepository.recordsWithSyncStatus(PENDING),
            prescriptionRepository.recordsWithSyncStatus(PENDING),
            medicalHistoryRepository.recordsWithSyncStatus(PENDING)
        ) { patientRecords, bpRecords, appointmentRecords, prescribedDrugRecords, medicalHistoryRecords ->
          PendingRecords(
              pendingPatientRecords = patientRecords,
              pendingBpRecords = bpRecords,
              pendingAppointmentRecords = appointmentRecords,
              pendingPrescribedDrugRecords = prescribedDrugRecords,
              pendingMedicalHistoryRecords = medicalHistoryRecords
          )
        }
        .filter { pendingRecords -> pendingRecords.hasPendingRecords }
        .flatMapCompletable { pendingRecords ->
          Completable.fromAction {
            Analytics.reportDataCleared(
                patientCount = pendingRecords.pendingPatientRecords.size,
                bloodPressureCount = pendingRecords.pendingBpRecords.size,
                appointmentCount = pendingRecords.pendingAppointmentRecords.size,
                prescribedDrugCount = pendingRecords.pendingPrescribedDrugRecords.size,
                medicalHistoryCount = pendingRecords.pendingMedicalHistoryRecords.size,
                since = pendingRecords.since!!
            )
          }
        }
  }

  private data class PendingRecords(
      val pendingPatientRecords: List<PatientProfile>,
      val pendingBpRecords: List<BloodPressureMeasurement>,
      val pendingAppointmentRecords: List<Appointment>,
      val pendingPrescribedDrugRecords: List<PrescribedDrug>,
      val pendingMedicalHistoryRecords: List<MedicalHistory>
  ) {
    val hasPendingRecords = (pendingPatientRecords.size +
        pendingBpRecords.size +
        pendingAppointmentRecords.size +
        pendingPrescribedDrugRecords.size +
        pendingMedicalHistoryRecords.size) > 0

    val since: Instant? = (pendingPatientRecords.map { it.patient.updatedAt } +
        pendingBpRecords.map { it.updatedAt } +
        pendingAppointmentRecords.map { it.updatedAt } +
        pendingPrescribedDrugRecords.map { it.updatedAt } +
        pendingMedicalHistoryRecords.map { it.updatedAt }).min()
  }
}
