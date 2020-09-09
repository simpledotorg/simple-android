package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.simple.clinic.R
import org.simple.clinic.di.injector
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import javax.inject.Inject

class TeleconsultNotRecordedDialog : DialogFragment() {

  @Inject
  lateinit var screenRouter: ScreenRouter

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
    return AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.teleconsultnotrecordeddialog_title))
        .setMessage(getString(R.string.teleconsultnotrecordeddialog_body))
        .setPositiveButton(getString(R.string.teleconsultnotrecordeddialog_positive_button_text)) { _, _ ->
          navigateToHomeScreen()
        }
        .setNegativeButton(getString(R.string.teleconsultnotrecordeddialog_negative_button_text), null)
        .create()
  }

  private fun navigateToHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey, direction = RouterDirection.BACKWARD)
  }

  interface Injector {
    fun inject(target: TeleconsultNotRecordedDialog)
  }
}
