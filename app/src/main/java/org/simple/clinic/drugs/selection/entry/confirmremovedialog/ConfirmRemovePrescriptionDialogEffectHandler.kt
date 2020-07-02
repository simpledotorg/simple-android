package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class ConfirmRemovePrescriptionDialogEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): ConfirmRemovePrescriptionDialogEffectHandler
  }

  fun build(): ObservableTransformer<ConfirmRemovePrescriptionDialogEffect,
      ConfirmRemovePrescriptionDialogEvent> = RxMobius
      .subtypeEffectHandler<ConfirmRemovePrescriptionDialogEffect,
          ConfirmRemovePrescriptionDialogEvent>()
      .addAction(CloseDialog::class.java, uiActions::closeDialog, schedulersProvider.ui())
      .build()
}
