package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.DatabaseMigrationAndroidTest
import org.simple.clinic.FakerModule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.bp.BloodPressureRepositoryAndroidTest
import org.simple.clinic.bp.sync.BloodPressureSyncAndroidTest
import org.simple.clinic.drugs.PrescriptionRepositoryAndroidTest
import org.simple.clinic.drugs.sync.PrescriptionSyncAndroidTest
import org.simple.clinic.encounter.EncounterRepositoryAndroidTest
import org.simple.clinic.encounter.sync.EncounterSyncAndroidTest
import org.simple.clinic.facility.FacilityRepositoryAndroidTest
import org.simple.clinic.help.HelpSyncAndroidTest
import org.simple.clinic.medicalhistory.MedicalHistoryRepositoryAndroidTest
import org.simple.clinic.medicalhistory.MedicalHistorySyncAndroidTest
import org.simple.clinic.overdue.AppointmentRepositoryAndroidTest
import org.simple.clinic.overdue.AppointmentSyncAndroidTest
import org.simple.clinic.patient.PatientRepositoryAndroidTest
import org.simple.clinic.patient.PatientSyncAndroidTest
import org.simple.clinic.patient.businessid.BusinessIdMetaDataAdapterAndroidTest
import org.simple.clinic.protocolv2.ProtocolRepositoryAndroidTest
import org.simple.clinic.protocolv2.sync.ProtocolSyncAndroidTest
import org.simple.clinic.reports.ReportsSyncAndroidTest
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.rules.RegisterPatientRule
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.security.pin.BruteForceProtectionAndroidTest
import org.simple.clinic.storage.DaoWithUpsertAndroidTest
import org.simple.clinic.storage.files.AndroidFileStorageAndroidTest
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepositoryAndroidTest
import org.simple.clinic.user.OngoingLoginEntryRepositoryTest
import org.simple.clinic.user.UserDaoAndroidTest
import org.simple.clinic.user.UserSessionAndroidTest
import org.simple.clinic.user.UserSessionServerIntegrationTest
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapterAndroidTest

@AppScope
@Component(
    modules = [
      AppModule::class,
      FakerModule::class,
      TestClockModule::class
    ]
)
interface TestAppComponent : AppComponent {

  fun inject(target: TestClinicApp)
  fun inject(target: UserSessionAndroidTest)
  fun inject(target: PrescriptionSyncAndroidTest)
  fun inject(target: BloodPressureSyncAndroidTest)
  fun inject(target: ProtocolSyncAndroidTest)
  fun inject(target: PatientRepositoryAndroidTest)
  fun inject(target: PrescriptionRepositoryAndroidTest)
  fun inject(target: FacilityRepositoryAndroidTest)
  fun inject(target: UserDaoAndroidTest)
  fun inject(target: AppointmentSyncAndroidTest)
  fun inject(target: AppointmentRepositoryAndroidTest)
  fun inject(target: LocalAuthenticationRule)
  fun inject(target: MedicalHistorySyncAndroidTest)
  fun inject(target: MedicalHistoryRepositoryAndroidTest)
  fun inject(target: PatientSyncAndroidTest)
  fun inject(target: BloodPressureRepositoryAndroidTest)
  fun inject(target: OngoingLoginEntryRepositoryTest)
  fun inject(target: BruteForceProtectionAndroidTest)
  fun inject(target: DaoWithUpsertAndroidTest)
  fun inject(target: ProtocolRepositoryAndroidTest)
  fun inject(target: RegisterPatientRule)
  fun inject(target: DatabaseMigrationAndroidTest)
  fun inject(target: AndroidFileStorageAndroidTest)
  fun inject(target: ReportsSyncAndroidTest)
  fun inject(target: MissingPhoneReminderRepositoryAndroidTest)
  fun inject(target: BusinessIdMetaDataAdapterAndroidTest)
  fun inject(target: HelpSyncAndroidTest)
  fun inject(target: IdentifierDisplayAdapterAndroidTest)
  fun inject(target: ServerAuthenticationRule)
  fun inject(target: UserSessionServerIntegrationTest)
  fun inject(target: EncounterRepositoryAndroidTest)
  fun inject(target: EncounterSyncAndroidTest)
}
