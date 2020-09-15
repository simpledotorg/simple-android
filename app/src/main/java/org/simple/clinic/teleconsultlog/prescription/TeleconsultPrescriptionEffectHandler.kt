package org.simple.clinic.teleconsultlog.prescription

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class TeleconsultPrescriptionEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: TeleconsultPrescriptionUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: TeleconsultPrescriptionUiActions): TeleconsultPrescriptionEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultPrescriptionEffect, TeleconsultPrescriptionEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultPrescriptionEffect, TeleconsultPrescriptionEvent>()
        .addTransformer(LoadPatientDetails::class.java, loadPatientDetails())
        .addAction(GoBack::class.java, uiActions::goBackToPreviousScreen, schedulersProvider.ui())
        .build()
  }

  private fun loadPatientDetails(): ObservableTransformer<LoadPatientDetails, TeleconsultPrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { patientRepository.patientImmediate(it.patientUuid) }
          .map(::PatientDetailsLoaded)
    }
  }
}
