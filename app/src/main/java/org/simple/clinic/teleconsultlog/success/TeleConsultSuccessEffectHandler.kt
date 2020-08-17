package org.simple.clinic.teleconsultlog.success

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.LoadPatientDetails
import org.simple.clinic.util.scheduler.SchedulersProvider

class TeleConsultSuccessEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(): TeleConsultSuccessEffectHandler
  }

  fun build(): ObservableTransformer<TeleConsultSuccessEffect, TeleConsultSuccessEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleConsultSuccessEffect, TeleConsultSuccessEvent>()
        .addTransformer(LoadPatientDetails::class.java, loadPatientDetails(schedulersProvider.io()))
        .build()
  }

  private fun loadPatientDetails(scheduler: Scheduler): ObservableTransformer<TeleConsultSuccessEffect.LoadPatientDetails, TeleConsultSuccessEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { patientRepository.patientImmediate(it.patientUuid) }
          .map(::PatientDetailsLoaded)
    }
  }
}
