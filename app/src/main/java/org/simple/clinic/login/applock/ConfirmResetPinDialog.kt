package org.simple.clinic.login.applock

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.user.UserSession
import javax.inject.Inject

class ConfirmResetPinDialog : AppCompatDialogFragment() {

  @Inject
  lateinit var userSession: UserSession

  companion object {
    fun show(fragmentManager: FragmentManager) {
      (fragmentManager.findFragmentByTag("confirm_reset_pin_alert") as ConfirmResetPinDialog?)?.dismiss()

      val fragment = ConfirmResetPinDialog()
      // Cancellable on the dialog builder is ignored. We have to use this.
      fragment.isCancelable = false
      fragment.show(fragmentManager, "confirm_reset_pin_alert")
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    TheActivity.component.inject(this)
  }


  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(context!!)
        .setTitle(R.string.applock_reset_pin_alert_title)
        .setMessage(R.string.applock_reset_pin_alert_message)
        .setPositiveButton(R.string.applock_reset_pin_alert_confirm) { _, _ ->
          val view = dialog.ownerActivity.findViewById<View>(android.R.id.content)
          val snackbar = Snackbar.make(view, getString(R.string.applock_reset_pin_waiting), Snackbar.LENGTH_INDEFINITE)
          snackbar.show()

          userSession.startForgotPinFlow(1)
              .observeOn(mainThread())
              .doOnTerminate { snackbar.dismiss() }
              .subscribe {
                // TODO: Open new PIN entry screen
              }
        }
        .setNegativeButton(R.string.applock_reset_pin_alert_cancel, null)
        .create()
  }
}
