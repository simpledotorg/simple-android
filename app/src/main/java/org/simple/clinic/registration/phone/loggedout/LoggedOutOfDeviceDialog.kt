package org.simple.clinic.registration.phone.loggedout

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.Observable
import org.simple.clinic.R
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class LoggedOutOfDeviceDialog : AppCompatDialogFragment(), LoggedOutOfDeviceDialogUi {

  companion object {
    private const val FRAGMENT_TAG = "LoggedOutOfDeviceDialog"

    fun show(fragmentManager: FragmentManager) {
      val existingFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commit()
      }

      val fragment = LoggedOutOfDeviceDialog().apply {
        isCancelable = false
      }

      fragmentManager
          .beginTransaction()
          .add(fragment, FRAGMENT_TAG)
          .commit()
    }
  }

  @Inject
  lateinit var effectHandler: LoggedOutOfDeviceEffectHandler

  private val okayButton: Button by unsafeLazy {
    (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
  }

  private val delegate by unsafeLazy {
    val uiRenderer = LoggedOutOfDeviceUiRenderer(this)

    MobiusDelegate.forActivity(
        events = Observable.never(),
        defaultModel = LoggedOutOfDeviceModel.create(),
        init = LoggedOutOfDeviceInit(),
        update = LoggedOutOfDeviceUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.registration_loggedout_dialog_title)
        .setMessage(R.string.registration_loggedout_dialog_message)
        .setPositiveButton(R.string.registration_loggedout_dialog_confirm, null)
        .create()
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun enableOkayButton() {
    okayButton.isEnabled = true
  }

  override fun disableOkayButton() {
    okayButton.isEnabled = false
  }

  interface Injector {
    fun inject(target: LoggedOutOfDeviceDialog)
  }
}
