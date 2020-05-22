package org.simple.clinic.editpatient.deletepatient

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class DeletePatientEffectHandler @AssistedInject constructor(
    val schedulersProvider: SchedulersProvider,
    @Assisted val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): DeletePatientEffectHandler
  }

  fun build(): ObservableTransformer<DeletePatientEffect, DeletePatientEvent> {
    return RxMobius
        .subtypeEffectHandler<DeletePatientEffect, DeletePatientEvent>()
        .addConsumer(ShowConfirmDeleteDialog::class.java, { uiActions.showConfirmDeleteDialog(it.patientName, it.deletedReason) }, schedulersProvider.ui())
        .build()
  }
}
