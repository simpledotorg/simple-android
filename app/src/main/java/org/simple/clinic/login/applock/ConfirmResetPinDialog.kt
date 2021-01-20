package org.simple.clinic.login.applock

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import org.simple.clinic.R
import org.simple.clinic.di.injector
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

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Simple_MaterialAlertDialog_Destructive)
        .setTitle(R.string.applock_reset_pin_alert_title)
        .setMessage(R.string.applock_reset_pin_alert_message)
        .setPositiveButton(R.string.applock_reset_pin_alert_confirm) { _, _ -> screenRouter.push(ForgotPinCreateNewPinScreenKey()) }
        .setNegativeButton(R.string.applock_reset_pin_alert_cancel, null)
        .create()
  }

  interface Injector {
    fun inject(target: ConfirmResetPinDialog)
  }
}
