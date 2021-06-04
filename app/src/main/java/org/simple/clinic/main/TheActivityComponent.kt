package org.simple.clinic.main

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import io.reactivex.Observable
import org.simple.clinic.activity.ActivityLifecycle
import org.simple.clinic.activity.RxActivityLifecycle
import org.simple.clinic.bloodsugar.history.BloodSugarHistoryScreenInjector
import org.simple.clinic.bp.history.BloodPressureHistoryScreenInjector
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.contactpatient.views.SetAppointmentReminderView
import org.simple.clinic.datepicker.calendar.CalendarDatePicker
import org.simple.clinic.deniedaccess.AccessDeniedScreenInjector
import org.simple.clinic.di.PagingModule
import org.simple.clinic.drugs.selection.EditMedicinesScreen
import org.simple.clinic.editpatient.ConfirmDiscardChangesDialog
import org.simple.clinic.editpatient.EditPatientScreen
import org.simple.clinic.editpatient.deletepatient.DeletePatientScreenInjector
import org.simple.clinic.enterotp.EnterOtpScreen
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.change.FacilityChangeScreen
import org.simple.clinic.facility.change.confirm.ConfirmFacilityChangeSheet
import org.simple.clinic.facilitypicker.FacilityPickerView
import org.simple.clinic.forgotpin.confirmpin.ForgotPinConfirmPinScreen
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreen
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.home.help.HelpScreen
import org.simple.clinic.home.overdue.OverdueScreen
import org.simple.clinic.home.patients.PatientsModule
import org.simple.clinic.home.patients.PatientsTabScreen
import org.simple.clinic.home.report.ReportsScreen
import org.simple.clinic.instantsearch.InstantSearchScreen
import org.simple.clinic.login.applock.AppLockScreen
import org.simple.clinic.login.applock.ConfirmResetPinDialog
import org.simple.clinic.medicalhistory.newentry.NewMedicalHistoryScreen
import org.simple.clinic.navigation.di.FragmentScreenKeyModule
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.newentry.PatientEntryScreen
import org.simple.clinic.newentry.country.di.InputFieldsFactoryModule
import org.simple.clinic.onboarding.OnboardingScreenInjector
import org.simple.clinic.recentpatient.RecentPatientsScreen
import org.simple.clinic.recentpatientsview.RecentPatientsView
import org.simple.clinic.removeoverdueappointment.RemoveOverdueAppointmentScreen
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.scanid.ScanSimpleIdScreen
import org.simple.clinic.scanid.scannedqrcode.ScannedQrCodeSheet
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.security.pin.PinEntryCardView
import org.simple.clinic.settings.SettingsScreen
import org.simple.clinic.settings.changelanguage.ChangeLanguageScreen
import org.simple.clinic.summary.PatientSummaryScreen
import org.simple.clinic.summary.addphone.AddPhoneNumberDialog
import org.simple.clinic.summary.assignedfacility.AssignedFacilityView
import org.simple.clinic.summary.bloodpressures.view.BloodPressureSummaryViewInjector
import org.simple.clinic.summary.bloodsugar.view.BloodSugarSummaryViewInjector
import org.simple.clinic.summary.linkId.LinkIdWithPatientSheet
import org.simple.clinic.summary.medicalhistory.MedicalHistorySummaryViewInjector
import org.simple.clinic.summary.prescribeddrugs.DrugSummaryViewInjector
import org.simple.clinic.summary.updatephone.UpdatePhoneNumberDialog
import org.simple.clinic.sync.indicator.SyncIndicatorView
import org.simple.clinic.teleconsultlog.prescription.TeleconsultPrescriptionScreen
import org.simple.clinic.teleconsultlog.prescription.doctorinfo.TeleconsultDoctorInfoView
import org.simple.clinic.teleconsultlog.prescription.medicines.TeleconsultMedicinesView
import org.simple.clinic.teleconsultlog.prescription.patientinfo.TeleconsultPatientInfoView
import org.simple.clinic.teleconsultlog.shareprescription.TeleconsultSharePrescriptionScreen
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessScreen
import org.simple.clinic.teleconsultlog.teleconsultrecord.screen.TeleconsultNotRecordedDialog
import org.simple.clinic.teleconsultlog.teleconsultrecord.screen.TeleconsultRecordScreen
import org.simple.clinic.textInputdatepicker.TextInputDatePickerSheet
import org.simple.clinic.widgets.PatientSearchResultItemView

@Subcomponent(modules = [TheActivityModule::class])
interface TheActivityComponent :
    OnboardingScreenInjector,
    MedicalHistorySummaryViewInjector,
    DrugSummaryViewInjector,
    BloodSugarSummaryViewInjector,
    BloodPressureHistoryScreenInjector,
    BloodPressureSummaryViewInjector,
    BloodSugarHistoryScreenInjector,
    AccessDeniedScreenInjector,
    PinEntryCardView.Injector,
    EnterOtpScreen.Injector,
    DeletePatientScreenInjector,
    PatientsTabScreen.Injector,
    HelpScreen.Injector,
    ReportsScreen.Injector,
    AddPhoneNumberDialog.Injector,
    RecentPatientsView.Injector,
    AssignedFacilityView.Injector,
    RecentPatientsScreen.Injector,
    FacilityPickerView.Injector,
    ForgotPinCreateNewPinScreen.Injector,
    OverdueScreen.Injector,
    ForgotPinConfirmPinScreen.Injector,
    HomeScreen.Injector,
    PatientEntryScreen.Injector,
    UpdatePhoneNumberDialog.Injector,
    EditPatientScreen.Injector,
    LinkIdWithPatientSheet.Injector,
    AppLockScreen.Injector,
    TeleConsultSuccessScreen.Injector,
    TeleconsultRecordScreen.Injector,
    TeleconsultNotRecordedDialog.Injector,
    TeleconsultPrescriptionScreen.Injector,
    TeleconsultPatientInfoView.Injector,
    TeleconsultMedicinesView.Injector,
    TeleconsultDoctorInfoView.Injector,
    PatientSummaryScreen.Injector,
    ConfirmResetPinDialog.Injector,
    NewMedicalHistoryScreen.Injector,
    ConfirmDiscardChangesDialog.Injector,
    EditMedicinesScreen.Injector,
    SyncIndicatorView.Injector,
    SettingsScreen.Injector,
    ChangeLanguageScreen.Injector,
    TeleconsultSharePrescriptionScreen.Injector,
    PatientSearchResultItemView.Injector,
    InstantSearchScreen.Injector,
    ScanSimpleIdScreen.Injector,
    ScannedQrCodeSheet.Injector,
    AlertFacilityChangeSheet.Injector,
    FacilityChangeScreen.Injector,
    ConfirmFacilityChangeSheet.Injector,
    ContactPatientBottomSheet.Injector,
    SetAppointmentReminderView.Injector,
    ScheduleAppointmentSheet.Injector,
    CalendarDatePicker.Injector,
    TextInputDatePickerSheet.Injector,
    RemoveOverdueAppointmentScreen.Injector {
  fun inject(target: TheActivity)

  @Subcomponent.Factory
  interface Factory {
    fun create(
        @BindsInstance activity: AppCompatActivity,
        @BindsInstance router: Router,
        @BindsInstance screenResults: ScreenResultBus
    ): TheActivityComponent
  }
}

@Module(includes = [
  PatientsModule::class,
  PagingModule::class,
  InputFieldsFactoryModule::class,
  FragmentScreenKeyModule::class
])
class TheActivityModule {

  @Provides
  fun theActivityLifecycle(activity: AppCompatActivity): Observable<ActivityLifecycle> {
    return RxActivityLifecycle.from(activity).stream()
  }

  @Provides
  fun fragmentManager(activity: AppCompatActivity): FragmentManager = activity.supportFragmentManager
}
