package org.simple.clinic.appupdate.dialog

import android.app.Dialog
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.simple.clinic.BuildConfig
import org.simple.clinic.R

class AppUpdateDialog : DialogFragment() {

  companion object {

    private const val FRAGMENT_TAG = "app_update_alert"

    fun show(fragmentManager: FragmentManager) {
      val fragment = AppUpdateDialog().apply {
        isCancelable = false
      }

      fragment.show(fragmentManager, FRAGMENT_TAG)
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.appupdatedialog_title))
        .setMessage(getString(R.string.appupdatedialog_body))
        .setPositiveButton(getString(R.string.appupdatedialog_positive_button_text)) { _, _ ->
          launchPlayStoreForUpdate()
        }
        .setNegativeButton(getString(R.string.appupdatedialog_negative_button_text), null)
        .create()
  }

  private fun launchPlayStoreForUpdate() {
    val intent = Intent(ACTION_VIEW).apply {
      data = Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
    }
    startActivity(intent)
  }
}
