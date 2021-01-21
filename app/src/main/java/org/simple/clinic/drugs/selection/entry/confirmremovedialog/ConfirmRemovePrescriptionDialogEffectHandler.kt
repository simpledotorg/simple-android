package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class ConfirmRemovePrescriptionDialogEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val prescriptionRepository: PrescriptionRepository,
    @Assisted private val uiActions: UiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: UiActions): ConfirmRemovePrescriptionDialogEffectHandler
  }

  fun build(): ObservableTransformer<ConfirmRemovePrescriptionDialogEffect,
      ConfirmRemovePrescriptionDialogEvent> = RxMobius
      .subtypeEffectHandler<ConfirmRemovePrescriptionDialogEffect,
          ConfirmRemovePrescriptionDialogEvent>()
      .addAction(CloseDialog::class.java, uiActions::closeDialog, schedulersProvider.ui())
      .addTransformer(RemovePrescription::class.java, removePrescription())
      .build()

  private fun removePrescription(): ObservableTransformer<RemovePrescription, ConfirmRemovePrescriptionDialogEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap {
            prescriptionRepository.softDeletePrescription(it.prescriptionUuid)
                .andThen(Observable.just(PrescriptionRemoved))
          }
    }
  }
}
