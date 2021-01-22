package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.simple.clinic.R
import org.simple.clinic.di.injector
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.navigation.v2.Router
import javax.inject.Inject

class TeleconsultNotRecordedDialog : DialogFragment() {

  @Inject
  lateinit var router: Router

  companion object {
    private const val FRAGMENT_TAG = "teleconsult_not_recorded_alert"

    fun show(fragmentManager: FragmentManager) {
      TeleconsultNotRecordedDialog().show(fragmentManager, FRAGMENT_TAG)
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.teleconsultnotrecordeddialog_title))
        .setMessage(getString(R.string.teleconsultnotrecordeddialog_body))
        .setPositiveButton(getString(R.string.teleconsultnotrecordeddialog_positive_button_text)) { _, _ ->
          navigateToHomeScreen()
        }
        .setNegativeButton(getString(R.string.teleconsultnotrecordeddialog_negative_button_text), null)
        .create()
  }

  private fun navigateToHomeScreen() {
    router.clearHistoryAndPush(HomeScreenKey)
  }

  interface Injector {
    fun inject(target: TeleconsultNotRecordedDialog)
  }
}
