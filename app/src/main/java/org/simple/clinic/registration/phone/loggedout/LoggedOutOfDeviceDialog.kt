package org.simple.clinic.registration.phone.loggedout

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class LoggedOutOfDeviceDialog : AppCompatDialogFragment() {

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
  lateinit var controller: LoggedOutOfDeviceDialogController

  private val okayButton: Button by unsafeLazy {
    (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
  }

  private val screenDestroys = PublishSubject.create<ScreenDestroyed>()
  private val onStarts = PublishSubject.create<Any>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    TheActivity.component.inject(this)
  }

  @SuppressLint("CheckResult")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = AlertDialog.Builder(requireContext())
        .setTitle(R.string.registration_loggedout_dialog_title)
        .setMessage(R.string.registration_loggedout_dialog_message)
        .setPositiveButton(R.string.registration_loggedout_dialog_confirm, null)
        .create()

    onStarts
        .take(1)
        .subscribe { setupDialog() }

    return dialog
  }

  private fun setupDialog() {
    bindUiToController(
        ui = this,
        events = screenCreates(),
        controller = controller,
        screenDestroys = screenDestroys
    )
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated())

  override fun onStart() {
    super.onStart()
    onStarts.onNext(Any())
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(ScreenDestroyed())
  }

  fun enableOkayButton() {
    okayButton.isEnabled = true
  }

  fun disableOkayButton() {
    okayButton.isEnabled = false
  }
}
