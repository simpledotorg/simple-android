package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.ClinicApp
import org.simple.clinic.bloodsugar.entry.di.BloodSugarEntryComponent
import org.simple.clinic.bloodsugar.selection.type.di.BloodSugarTypePickerSheetComponent
import org.simple.clinic.bp.entry.di.BloodPressureEntryComponent
import org.simple.clinic.drugs.selection.dosage.di.DosagePickerSheetComponent
import org.simple.clinic.drugs.selection.entry.di.CustomPrescriptionEntrySheetComponent
import org.simple.clinic.home.overdue.appointmentreminder.di.AppointmentReminderSheetComponent
import org.simple.clinic.home.overdue.phonemask.di.PhoneMaskBottomSheetComponent
import org.simple.clinic.home.overdue.removepatient.di.RemoveAppointmentScreenComponent
import org.simple.clinic.login.OtpSmsReceiver
import org.simple.clinic.main.TheActivityComponent
import org.simple.clinic.newentry.clearbutton.ClearFieldImageButton
import org.simple.clinic.scheduleappointment.di.ScheduleAppointmentSheetComponent
import org.simple.clinic.scheduleappointment.facilityselection.FacilitySelectionActivityComponent
import org.simple.clinic.setup.SetupActivityComponent
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncWorker
import javax.inject.Scope

@AppScope
@Component(modules = [(AppModule::class)])
interface AppComponent {

  fun inject(target: ClinicApp)
  fun inject(target: SyncWorker)
  fun inject(target: ClearFieldImageButton)
  fun inject(target: OtpSmsReceiver)
  fun inject(target: DataSync)

  fun theActivityComponentBuilder(): TheActivityComponent.Builder
  fun setupActivityComponentBuilder(): SetupActivityComponent.Builder
  fun patientFacilityChangeComponentBuilder() : FacilitySelectionActivityComponent.Builder
  fun bloodSugarEntryComponent(): BloodSugarEntryComponent.Builder
  fun bloodPressureEntryComponent(): BloodPressureEntryComponent.Builder
  fun appointmentReminderSheetComponent(): AppointmentReminderSheetComponent.Builder
  fun scheduleAppointmentSheetComponentBuilder(): ScheduleAppointmentSheetComponent.Builder
  fun dosagePickerSheetComponentBuilder(): DosagePickerSheetComponent.Builder
  fun bloodSugarTypePickerSheetComponentBuilder(): BloodSugarTypePickerSheetComponent.Builder
  fun phoneMaskBottomSheetComponentBuilder(): PhoneMaskBottomSheetComponent.Builder
  fun customPrescriptionEntrySheetComponentBuilder(): CustomPrescriptionEntrySheetComponent.Builder
  fun removeAppointmentScreenComponentBuilder(): RemoveAppointmentScreenComponent.Builder
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope
