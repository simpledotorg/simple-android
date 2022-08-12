package org.simple.clinic.registration.phone.loggedout

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class LoggedOutOfDeviceDialog : BaseDialog<
    LoggedOutOfDeviceDialog.Key,
    Nothing,
    LoggedOutOfDeviceModel,
    LoggedOutOfDeviceEvent,
    LoggedOutOfDeviceEffect,
    Nothing>(), LoggedOutOfDeviceDialogUi {

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

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?): Nothing? {
    return null
  }

  override fun defaultModel() = LoggedOutOfDeviceModel.create()

  override fun events(): Observable<LoggedOutOfDeviceEvent> = Observable.never()

  override fun uiRenderer() = LoggedOutOfDeviceUiRenderer(this)

  override fun createInit() = LoggedOutOfDeviceInit()

  override fun createUpdate() = LoggedOutOfDeviceUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Nothing>) = effectHandler
      .build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.registration_loggedout_dialog_title)
        .setMessage(R.string.registration_loggedout_dialog_message)
        .setPositiveButton(R.string.registration_loggedout_dialog_confirm, null)
        .create()
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

  @Parcelize
  data class Key(
      override val type: ScreenType = ScreenType.Modal,
      override val analyticsName: String = "Logged Out Of Device Dialog"
  ) : ScreenKey() {

    override fun instantiateFragment() = LoggedOutOfDeviceDialog().apply {
      isCancelable = false
    }
  }
}
