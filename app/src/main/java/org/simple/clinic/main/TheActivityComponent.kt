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
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityView
import org.simple.clinic.bloodsugar.history.BloodSugarHistoryScreenInjector
import org.simple.clinic.bp.history.BloodPressureHistoryScreenInjector
import org.simple.clinic.deniedaccess.AccessDeniedScreenInjector
import org.simple.clinic.di.PagingModule
import org.simple.clinic.drugs.selection.EditMedicinesScreen
import org.simple.clinic.editpatient.ConfirmDiscardChangesDialog
import org.simple.clinic.editpatient.EditPatientScreen
import org.simple.clinic.editpatient.deletepatient.DeletePatientScreenInjector
import org.simple.clinic.enterotp.EnterOtpScreen
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
import org.simple.clinic.search.PatientSearchScreen
import org.simple.clinic.search.results.PatientSearchResultsScreen
import org.simple.clinic.searchresultsview.PatientSearchView
import org.simple.clinic.security.pin.PinEntryCardView
import org.simple.clinic.settings.SettingsScreen
import org.simple.clinic.settings.changelanguage.ChangeLanguageScreen
import org.simple.clinic.shortcodesearchresult.ShortCodeSearchResultScreen
import org.simple.clinic.summary.PatientSummaryScreen
import org.simple.clinic.summary.addphone.AddPhoneNumberDialog
import org.simple.clinic.summary.assignedfacility.AssignedFacilityView
import org.simple.clinic.summary.bloodpressures.view.BloodPressureSummaryViewInjector
import org.simple.clinic.summary.bloodsugar.view.BloodSugarSummaryViewInjector
import org.simple.clinic.summary.linkId.LinkIdWithPatientView
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
import org.simple.clinic.widgets.PatientSearchResultItemView
import org.simple.clinic.widgets.PatientSearchResultItemView_Old

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
    PatientSearchResultsScreen.Injector,
    PatientSearchScreen.Injector,
    ForgotPinConfirmPinScreen.Injector,
    HomeScreen.Injector,
    PatientEntryScreen.Injector,
    UpdatePhoneNumberDialog.Injector,
    EditPatientScreen.Injector,
    LinkIdWithPatientView.Injector,
    AppLockScreen.Injector,
    PatientSearchView.Injector,
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
    PatientSearchResultItemView_Old.Injector,
    AllPatientsInFacilityView.Injector,
    ShortCodeSearchResultScreen.Injector,
    SettingsScreen.Injector,
    ChangeLanguageScreen.Injector,
    TeleconsultSharePrescriptionScreen.Injector,
    PatientSearchResultItemView.Injector,
    InstantSearchScreen.Injector {
  fun inject(target: TheActivity)

  @Subcomponent.Factory
  interface Factory {
    fun create(
        @BindsInstance activity: AppCompatActivity,
        @BindsInstance router: Router
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
