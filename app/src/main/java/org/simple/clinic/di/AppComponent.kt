package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.ClinicApp
import org.simple.clinic.bloodsugar.entry.di.BloodSugarEntryComponent
import org.simple.clinic.bloodsugar.selection.type.di.BloodSugarTypePickerSheetComponent
import org.simple.clinic.bp.entry.di.BloodPressureEntryComponent
import org.simple.clinic.deeplink.di.DeepLinkComponent
import org.simple.clinic.drugs.selection.dosage.di.DosagePickerSheetComponent
import org.simple.clinic.drugs.selection.entry.di.CustomPrescriptionEntrySheetComponent
import org.simple.clinic.login.OtpSmsReceiver
import org.simple.clinic.main.TheActivityComponent
import org.simple.clinic.registerorlogin.AuthenticationActivityComponent
import org.simple.clinic.setup.SetupActivityComponent
import org.simple.clinic.signature.SignatureComponent
import org.simple.clinic.summary.teleconsultation.contactdoctor.ContactDoctorComponent
import org.simple.clinic.summary.teleconsultation.status.TeleconsultStatusComponent
import org.simple.clinic.sync.DataSync
import org.simple.clinic.remoteconfig.UpdateRemoteConfigWorker
import org.simple.clinic.sync.SyncWorker
import org.simple.clinic.teleconsultlog.drugduration.di.DrugDurationComponent
import org.simple.clinic.teleconsultlog.medicinefrequency.di.MedicineFrequencyComponent
import javax.inject.Scope

@AppScope
@Component(modules = [(AppModule::class)])
interface AppComponent {

  fun inject(target: ClinicApp)
  fun inject(target: SyncWorker)
  fun inject(target: UpdateRemoteConfigWorker)
  fun inject(target: OtpSmsReceiver)
  fun inject(target: DataSync)

  fun theActivityComponent(): TheActivityComponent.Factory
  fun setupActivityComponent(): SetupActivityComponent.Factory
  fun bloodSugarEntryComponent(): BloodSugarEntryComponent.Factory
  fun bloodPressureEntryComponent(): BloodPressureEntryComponent.Factory
  fun dosagePickerSheetComponent(): DosagePickerSheetComponent.Factory
  fun bloodSugarTypePickerSheetComponent(): BloodSugarTypePickerSheetComponent.Factory
  fun customPrescriptionEntrySheetComponent(): CustomPrescriptionEntrySheetComponent.Factory
  fun deepLinkComponent(): DeepLinkComponent.Factory
  fun signatureComponent(): SignatureComponent.Factory
  fun drugDurationComponent(): DrugDurationComponent.Factory
  fun medicineFrequencyComponent(): MedicineFrequencyComponent.Factory
  fun contactDoctorComponent(): ContactDoctorComponent.Factory
  fun teleconsultStatusComponent(): TeleconsultStatusComponent.Factory
  fun authenticationActivityComponent(): AuthenticationActivityComponent.Factory
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope
