package org.simple.clinic.editpatient

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import javax.inject.Inject

class ConfirmDiscardChangesDialog : AppCompatDialogFragment() {

  @Inject
  lateinit var screenRouter: ScreenRouter

  companion object {
    fun show(fragmentManager: FragmentManager) {
      val fragment = ConfirmDiscardChangesDialog()
      fragment.show(fragmentManager, "confirm_discard_changes_alert")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    TheActivity.component.inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(context!!, R.style.Clinic_V2_DialogStyle_Destructive)
        .setMessage(R.string.patientedit_confirm_discard_title)
        .setPositiveButton(R.string.patientedit_confirm_discard_ok) { _, _ ->
          screenRouter.pop()
        }
        .setNegativeButton(R.string.patientedit_confirm_discard_cancel, null)
        .create()
  }
}
