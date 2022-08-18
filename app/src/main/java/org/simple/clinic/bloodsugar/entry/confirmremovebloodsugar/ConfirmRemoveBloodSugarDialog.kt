package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import java.util.UUID
import javax.inject.Inject

class ConfirmRemoveBloodSugarDialog : BaseDialog<
    ConfirmRemoveBloodSugarDialog.Key,
    Nothing,
    ConfirmRemoveBloodSugarModel,
    ConfirmRemoveBloodSugarEvent,
    ConfirmRemoveBloodSugarEffect,
    Nothing>(), ConfirmRemoveBloodSugarUiActions {

  @Inject
  lateinit var effectHandler: ConfirmRemoveBloodSugarEffectHandler.Factory

  @Inject
  lateinit var router: Router

  private val events = PublishSubject.create<ConfirmRemoveBloodSugarEvent>()

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

  override fun closeDialog() {
    router.popWithResult(Succeeded(BloodSugarRemoved))
  }

  @Parcelize
  data class Key(
      val bloodSugarUuid: UUID,
      override val analyticsName: String = "Confirm Remove Blood Sugar Dialog",
      override val type: ScreenType = ScreenType.Modal
  ) : ScreenKey() {

    override fun instantiateFragment() = ConfirmRemoveBloodSugarDialog()
  }

  @Parcelize
  object BloodSugarRemoved : Parcelable
}
