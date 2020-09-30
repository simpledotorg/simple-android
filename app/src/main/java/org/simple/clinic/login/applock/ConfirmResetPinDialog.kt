package org.simple.clinic.login.applock

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import org.simple.clinic.R
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreenKey
import org.simple.clinic.main.TheActivity
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.UserSession
import javax.inject.Inject

class ConfirmResetPinDialog : AppCompatDialogFragment() {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var patientRepository: PatientRepository

  companion object {
    fun show(fragmentManager: FragmentManager) {
      (fragmentManager.findFragmentByTag("confirm_reset_pin_alert") as ConfirmResetPinDialog?)?.dismiss()

      ConfirmResetPinDialog().show(fragmentManager, "confirm_reset_pin_alert")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    TheActivity.component.inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext(), R.style.Clinic_V2_DialogStyle_Destructive)
        .setTitle(R.string.applock_reset_pin_alert_title)
        .setMessage(R.string.applock_reset_pin_alert_message)
        .setPositiveButton(R.string.applock_reset_pin_alert_confirm) { _, _ -> screenRouter.push(ForgotPinCreateNewPinScreenKey()) }
        .setNegativeButton(R.string.applock_reset_pin_alert_cancel, null)
        .create()
  }
}
