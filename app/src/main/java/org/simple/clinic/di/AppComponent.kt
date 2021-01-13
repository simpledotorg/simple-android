package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.ClinicApp
import org.simple.clinic.bloodsugar.entry.di.BloodSugarEntryComponent
import org.simple.clinic.bloodsugar.selection.type.di.BloodSugarTypePickerSheetComponent
import org.simple.clinic.bp.assignbppassport.BpPassportSheetComponent
import org.simple.clinic.bp.entry.di.BloodPressureEntryComponent
import org.simple.clinic.contactpatient.di.ContactPatientBottomSheetComponent
import org.simple.clinic.deeplink.di.DeepLinkComponent
import org.simple.clinic.drugs.selection.dosage.di.DosagePickerSheetComponent
import org.simple.clinic.drugs.selection.entry.di.CustomPrescriptionEntrySheetComponent
import org.simple.clinic.facility.alertchange.AlertFacilityChangeComponent
import org.simple.clinic.facility.change.confirm.FacilityChangeComponent
import org.simple.clinic.facility.change.confirm.di.ConfirmFacilityChangeComponent
import org.simple.clinic.login.OtpSmsReceiver
import org.simple.clinic.main.TheActivityComponent
import org.simple.clinic.registerorlogin.AuthenticationActivityComponent
import org.simple.clinic.scanid.ScanBpPassportActivityComponent
import org.simple.clinic.scheduleappointment.di.ScheduleAppointmentSheetComponent
import org.simple.clinic.scheduleappointment.facilityselection.FacilitySelectionActivityComponent
import org.simple.clinic.setup.SetupActivityComponent
import org.simple.clinic.signature.SignatureComponent
import org.simple.clinic.summary.teleconsultation.contactdoctor.ContactDoctorComponent
import org.simple.clinic.summary.teleconsultation.status.TeleconsultStatusComponent
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncWorker
import org.simple.clinic.teleconsultlog.drugduration.di.DrugDurationComponent
import org.simple.clinic.teleconsultlog.medicinefrequency.di.MedicineFrequencyComponent
import javax.inject.Scope

@AppScope
@Component(modules = [(AppModule::class)])
interface AppComponent {

  fun inject(target: ClinicApp)
  fun inject(target: SyncWorker)
  fun inject(target: OtpSmsReceiver)
  fun inject(target: DataSync)

  fun theActivityComponent(): TheActivityComponent.Factory
  fun setupActivityComponent(): SetupActivityComponent.Factory
  fun facilitySelectionActivityComponent(): FacilitySelectionActivityComponent.Factory
  fun bloodSugarEntryComponent(): BloodSugarEntryComponent.Factory
  fun bloodPressureEntryComponent(): BloodPressureEntryComponent.Factory
  fun scheduleAppointmentSheetComponent(): ScheduleAppointmentSheetComponent.Factory
  fun dosagePickerSheetComponent(): DosagePickerSheetComponent.Factory
  fun bloodSugarTypePickerSheetComponent(): BloodSugarTypePickerSheetComponent.Factory
  fun customPrescriptionEntrySheetComponent(): CustomPrescriptionEntrySheetComponent.Factory
  fun confirmFacilityChangeComponent(): ConfirmFacilityChangeComponent.Factory
  fun facilityChangeComponent(): FacilityChangeComponent.Factory
  fun alertFacilityChangeComponent(): AlertFacilityChangeComponent.Factory
  fun patientContactBottomSheetComponent(): ContactPatientBottomSheetComponent.Factory
  fun deepLinkComponent(): DeepLinkComponent.Builder
  fun signatureComponent(): SignatureComponent.Builder
  fun drugDurationComponent(): DrugDurationComponent.Builder
  fun medicineFrequencyComponent(): MedicineFrequencyComponent.Builder
  fun contactDoctorComponent(): ContactDoctorComponent.Builder
  fun teleconsultStatusComponent(): TeleconsultStatusComponent.Builder
  fun authenticationActivityComponent(): AuthenticationActivityComponent.Builder
  fun scanBpPassportActivityComponent(): ScanBpPassportActivityComponent.Builder
  fun bpPassportSheetComponent(): BpPassportSheetComponent.Builder
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope
