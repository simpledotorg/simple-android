package org.simple.clinic.patientcontact

import android.content.Context
import android.os.Bundle
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.sheet_patientcontact.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.patient.Gender
import org.simple.clinic.patientcontact.di.PatientContactBottomSheetComponent
import org.simple.clinic.phone.Dialer
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import java.util.Locale
import javax.inject.Inject

class PatientContactBottomSheet : BottomSheetActivity(), PatientContactUi, PatientContactUiActions {

  @Inject
  lateinit var phoneCaller: PhoneCaller

  @Inject
  lateinit var locale: Locale

  private lateinit var component: PatientContactBottomSheetComponent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_patientcontact)
  }

  override fun onBackgroundClick() {
    finish()
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .patientContactBottomSheetComponent()
        .activity(this)
        .build()

    component.inject(this)
  }

  override fun renderPatientDetails(name: String, gender: Gender, age: Int, phoneNumber: String) {
    callPatientView.renderPatientDetails(name, gender, age, phoneNumber)
  }

  override fun showCallResultSection() {
    callPatientView.callResultSectionVisible = true
  }

  override fun hideCallResultSection() {
    callPatientView.callResultSectionVisible = false
  }

  override fun showSecureCallUi() {
    callPatientView.secureCallingSectionVisible = true
  }

  override fun hideSecureCallUi() {
    callPatientView.secureCallingSectionVisible = false
  }

  override fun directlyCallPatient(patientPhoneNumber: String, dialer: Dialer) {
    phoneCaller.normalCall(patientPhoneNumber, dialer)
  }

  override fun maskedCallPatient(patientPhoneNumber: String, proxyNumber: String, dialer: Dialer) {
    phoneCaller.secureCall(
        visibleNumber = proxyNumber,
        hiddenNumber = patientPhoneNumber,
        dialer = dialer
    )
  }
}
