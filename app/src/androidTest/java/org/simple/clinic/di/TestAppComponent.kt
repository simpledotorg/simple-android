package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.FakerModule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.bp.sync.BloodPressureSyncAndroidTest
import org.simple.clinic.drugs.PrescriptionRepositoryAndroidTest
import org.simple.clinic.drugs.sync.PrescriptionSyncAndroidTest
import org.simple.clinic.facility.FacilityRepositoryAndroidTest
import org.simple.clinic.facility.FacilitySyncAndroidTest
import org.simple.clinic.medicalhistory.MedicalHistoryRepositoryAndroidTest
import org.simple.clinic.medicalhistory.MedicalHistorySyncAndroidTest
import org.simple.clinic.overdue.AppointmentRepositoryAndroidTest
import org.simple.clinic.overdue.AppointmentSyncAndroidTest
import org.simple.clinic.overdue.communication.CommunicationRepositoryAndroidTest
import org.simple.clinic.overdue.communication.CommunicationSyncAndroidTest
import org.simple.clinic.patient.PatientRepositoryAndroidTest
import org.simple.clinic.patient.PatientSyncAndroidTest
import org.simple.clinic.summary.RelativeTimestampGeneratorAndroidTest
import org.simple.clinic.user.UserDaoAndroidTest
import org.simple.clinic.user.UserSessionAndroidTest

@AppScope
@Component(modules = [AppModule::class, FakerModule::class])
interface TestAppComponent : AppComponent {

  fun inject(target: TestClinicApp)
  fun inject(target: UserSessionAndroidTest)
  fun inject(target: PatientSyncAndroidTest)
  fun inject(target: FacilitySyncAndroidTest)
  fun inject(target: PrescriptionSyncAndroidTest)
  fun inject(target: BloodPressureSyncAndroidTest)
  fun inject(target: PatientRepositoryAndroidTest)
  fun inject(target: PrescriptionRepositoryAndroidTest)
  fun inject(target: FacilityRepositoryAndroidTest)
  fun inject(target: UserDaoAndroidTest)
  fun inject(target: AppointmentSyncAndroidTest)
  fun inject(target: CommunicationSyncAndroidTest)
  fun inject(target: AppointmentRepositoryAndroidTest)
  fun inject(target: CommunicationRepositoryAndroidTest)
  fun inject(target: AuthenticationRule)
  fun inject(target: MedicalHistorySyncAndroidTest)
  fun inject(target: MedicalHistoryRepositoryAndroidTest)
  fun inject(target: RelativeTimestampGeneratorAndroidTest)
}
