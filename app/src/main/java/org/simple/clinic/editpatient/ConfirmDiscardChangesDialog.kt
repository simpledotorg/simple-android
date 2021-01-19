package org.simple.clinic.editpatient

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import org.simple.clinic.R
import org.simple.clinic.di.injector
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

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Simple_MaterialAlertDialog_Destructive)
        .setMessage(R.string.patientedit_confirm_discard_title)
        .setPositiveButton(R.string.patientedit_confirm_discard_ok) { _, _ ->
          screenRouter.pop()
        }
        .setNegativeButton(R.string.patientedit_confirm_discard_cancel, null)
        .create()
  }

  interface Injector {
    fun inject(target: ConfirmDiscardChangesDialog)
  }
}
