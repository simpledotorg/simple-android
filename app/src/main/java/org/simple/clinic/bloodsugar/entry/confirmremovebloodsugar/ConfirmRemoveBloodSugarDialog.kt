package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import org.simple.clinic.util.unsafeLazy
import java.util.UUID
import javax.inject.Inject

class ConfirmRemoveBloodSugarDialog : BaseDialog<
    ConfirmRemoveBloodSugarDialog.Key,
    Nothing,
    ConfirmRemoveBloodSugarModel,
    ConfirmRemoveBloodSugarEvent,
    ConfirmRemoveBloodSugarEffect,
    Nothing>(), ConfirmRemoveBloodSugarUiActions {

  companion object {
    private const val KEY_BLOOD_SUGAR_UUID = "bloodSugarMeasurementUuid"

    fun show(bloodSugarMeasurementUuid: UUID, fragmentManager: FragmentManager) {
      val fragmentTag = "fragment_confirm_remove_blood_sugar"

      val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val args = Bundle()
      args.putSerializable(KEY_BLOOD_SUGAR_UUID, bloodSugarMeasurementUuid)

      val fragment = ConfirmRemoveBloodSugarDialog()
      fragment.arguments = args

      fragmentManager
          .beginTransaction()
          .add(fragment, fragmentTag)
          .commitNowAllowingStateLoss()
    }
  }

  @Inject
  lateinit var effectHandler: ConfirmRemoveBloodSugarEffectHandler.Factory

  private val events = PublishSubject.create<ConfirmRemoveBloodSugarEvent>()

  private val delegate by unsafeLazy {
    val bloodSugarMeasurementUuid = requireArguments().getSerializable(KEY_BLOOD_SUGAR_UUID) as UUID

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = ConfirmRemoveBloodSugarModel.create(bloodSugarMeasurementUuid),
        update = ConfirmRemoveBloodSugarUpdate(),
        effectHandler = effectHandler.create(this).build()
    )
  }

  private var removeBloodSugarListener: RemoveBloodSugarListener? = null

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?): Nothing? {
    return null
  }

  override fun defaultModel() = ConfirmRemoveBloodSugarModel.create(screenKey.bloodSugarUuid)

  override fun events(): Observable<ConfirmRemoveBloodSugarEvent> = events.cast()

  override fun createUpdate() = ConfirmRemoveBloodSugarUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Nothing>) = effectHandler
      .create(this)
      .build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<ConfirmRemoveBloodSugarDialogInjector>().inject(this)
    removeBloodSugarListener = parentFragment as? RemoveBloodSugarListener
    if (removeBloodSugarListener == null) {
      throw ClassCastException("$context must implement RemoveBloodSugarListener")
    }
  }

  override fun onDetach() {
    removeBloodSugarListener = null
    super.onDetach()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Simple_MaterialAlertDialog_Destructive)
        .setTitle(R.string.bloodsugarentry_remove_blood_sugar_title)
        .setMessage(R.string.bloodsugarentry_remove_blood_sugar_message)
        .setPositiveButton(R.string.bloodsugarentry_remove_blood_sugar_confirm) { _, _ ->
          events.onNext(RemoveBloodSugarClicked)
        }
        .setNegativeButton(R.string.bloodsugarentry_remove_blood_sugar_cancel, null)
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

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun closeDialog() {
    // TODO: Use pop with result once migrated to navigation v2
    removeBloodSugarListener?.onBloodSugarRemoved()
  }

  interface RemoveBloodSugarListener {
    fun onBloodSugarRemoved()
  }

  @Parcelize
  data class Key(
      val bloodSugarUuid: UUID,
      override val analyticsName: String = "Confirm Remove Blood Sugar Dialog",
      override val type: ScreenType = ScreenType.Modal
  ) : ScreenKey() {

    override fun instantiateFragment() = ConfirmRemoveBloodSugarDialog()
  }
}
