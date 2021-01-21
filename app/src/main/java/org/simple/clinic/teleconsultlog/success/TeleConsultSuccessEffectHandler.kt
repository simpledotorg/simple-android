package org.simple.clinic.teleconsultlog.success

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.GoToHomeScreen
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.GoToPrescriptionScreen
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.LoadPatientDetails
import org.simple.clinic.util.scheduler.SchedulersProvider

class TeleConsultSuccessEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    @Assisted private val uiActions: TeleConsultSuccessScreenUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: TeleConsultSuccessScreenUiActions): TeleConsultSuccessEffectHandler
  }

  fun build(): ObservableTransformer<TeleConsultSuccessEffect, TeleConsultSuccessEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleConsultSuccessEffect, TeleConsultSuccessEvent>()
        .addTransformer(LoadPatientDetails::class.java, loadPatientDetails(schedulersProvider.io()))
        .addAction(GoToHomeScreen::class.java, { uiActions.goToHomeScreen() }, schedulersProvider.ui())
        .addConsumer(GoToPrescriptionScreen::class.java, { uiActions.goToPrescriptionScreen(it.patientUuid, it.teleconsultRecordId) }, schedulersProvider.ui())
        .build()
  }

  private fun loadPatientDetails(scheduler: Scheduler): ObservableTransformer<LoadPatientDetails, TeleConsultSuccessEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { patientRepository.patientImmediate(it.patientUuid) }
          .map(::PatientDetailsLoaded)
    }
  }
}
