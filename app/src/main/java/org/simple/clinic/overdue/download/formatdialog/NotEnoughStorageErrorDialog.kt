package org.simple.clinic.overdue.download.formatdialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.navigation.v2.ScreenKey

class NotEnoughStorageErrorDialog : AppCompatDialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.not_enough_storage_error_dialog_title)
        .setMessage(R.string.not_enough_storage_error_dialog_message)
        .setPositiveButton(android.R.string.ok) { _, _ ->
          dismiss()
        }
        .create()
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Not Enough Storage Error Dialog",
      override val type: ScreenType = ScreenType.Modal
  ) : ScreenKey() {

    override fun instantiateFragment() = NotEnoughStorageErrorDialog()
  }
}
