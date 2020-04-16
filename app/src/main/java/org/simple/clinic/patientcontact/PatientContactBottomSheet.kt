package org.simple.clinic.patientcontact

import android.os.Bundle
import kotlinx.android.synthetic.main.sheet_patientcontact.*
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.phone.Dialer
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.widgets.BottomSheetActivity

class PatientContactBottomSheet: BottomSheetActivity(), PatientContactUi, PatientContactUiActions {

  lateinit var phoneCaller: PhoneCaller

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_patientcontact)
  }

  override fun onBackgroundClick() {
    finish()
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
