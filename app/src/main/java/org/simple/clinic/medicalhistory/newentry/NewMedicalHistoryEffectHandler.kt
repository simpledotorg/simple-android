package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class NewMedicalHistoryEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: NewMedicalHistoryUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: NewMedicalHistoryUiActions): NewMedicalHistoryEffectHandler
  }

  fun build(): ObservableTransformer<NewMedicalHistoryEffect, NewMedicalHistoryEvent> {
    return RxMobius
        .subtypeEffectHandler<NewMedicalHistoryEffect, NewMedicalHistoryEvent>()
        .addConsumer(OpenPatientSummaryScreen::class.java, { effect -> uiActions.openPatientSummaryScreen(effect.patientUuid)}, schedulersProvider.ui())
        .build()
  }
}
