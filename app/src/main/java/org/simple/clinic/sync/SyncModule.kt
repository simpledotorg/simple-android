package org.simple.clinic.sync

import dagger.Module
import dagger.Provides
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.di.BloodSugarModule
import org.simple.clinic.bloodsugar.sync.BloodSugarSync
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.bp.di.BloodPressureModule
import org.simple.clinic.bp.sync.BloodPressureSync
import org.simple.clinic.drugs.PrescriptionModule
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.drugs.sync.PrescriptionSync
import org.simple.clinic.facility.FacilityModule
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.help.HelpModule
import org.simple.clinic.help.HelpSync
import org.simple.clinic.medicalhistory.MedicalHistoryModule
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.overdue.AppointmentModule
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.AppointmentSync
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.patient.sync.PatientSyncModule
import org.simple.clinic.protocol.ProtocolModule
import org.simple.clinic.protocol.sync.ProtocolSync
import org.simple.clinic.remoteconfig.RemoteConfigSync
import org.simple.clinic.reports.ReportsModule
import org.simple.clinic.reports.ReportsSync
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationSync
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordSync
import javax.inject.Named

@Module(includes = [
  PatientSyncModule::class,
  BloodPressureModule::class,
  PrescriptionModule::class,
  FacilityModule::class,
  AppointmentModule::class,
  MedicalHistoryModule::class,
  BloodSugarModule::class,
  ProtocolModule::class,
  ReportsModule::class,
  HelpModule::class])
class SyncModule {

  @Provides
  fun syncs(
      facilitySync: FacilitySync,
      protocolSync: ProtocolSync,
      patientSync: PatientSync,
      bloodPressureSync: BloodPressureSync,
      medicalHistorySync: MedicalHistorySync,
      appointmentSync: AppointmentSync,
      prescriptionSync: PrescriptionSync,
      reportsSync: ReportsSync,
      remoteConfigSync: RemoteConfigSync,
      helpSync: HelpSync,
      bloodSugarSync: BloodSugarSync,
      teleconsultationMedicalOfficersSync: TeleconsultationSync,
      teleconsultRecordSync : TeleconsultRecordSync
  ): ArrayList<ModelSync> {
    return arrayListOf(
        facilitySync, protocolSync, patientSync,
        bloodPressureSync, medicalHistorySync, appointmentSync,
        prescriptionSync, reportsSync, remoteConfigSync, helpSync,
        bloodSugarSync, teleconsultationMedicalOfficersSync,
        teleconsultRecordSync
    )
  }

  @Provides
  @Named("frequently_syncing_repositories")
  fun frequentlySyncingRepositories(
      patientSyncRepository: PatientRepository,
      bloodPressureSyncRepository: BloodPressureRepository,
      medicalHistorySyncRepository: MedicalHistoryRepository,
      appointmentSyncRepository: AppointmentRepository,
      prescriptionSyncRepository: PrescriptionRepository,
      bloodSugarRepository: BloodSugarRepository
  ): ArrayList<SynceableRepository<*, *>> {
    return arrayListOf(
        patientSyncRepository,
        bloodPressureSyncRepository,
        medicalHistorySyncRepository,
        appointmentSyncRepository,
        prescriptionSyncRepository,
        bloodSugarRepository
    )
  }
}
