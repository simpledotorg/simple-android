package org.simple.clinic.bp.assignbppassport

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class BpPassportEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    @Assisted private val uiActions: BpPassportUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: BpPassportUiActions): BpPassportEffectHandler
  }

  fun build(): ObservableTransformer<BpPassportEffect, BpPassportEvent> {
    return RxMobius
        .subtypeEffectHandler<BpPassportEffect, BpPassportEvent>()
        .addTransformer(SaveNewOngoingPatientEntry::class.java, saveNewPatientEntry())
        .addConsumer(SendBlankBpPassportResult::class.java, { uiActions.sendBpPassportResult(it.bpPassportResult) }, schedulersProvider.ui())
        .build()
  }

  private fun saveNewPatientEntry(): ObservableTransformer<SaveNewOngoingPatientEntry, BpPassportEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { effect -> patientRepository.saveOngoingEntry(effect.entry) }
          .map { NewOngoingPatientEntrySaved }
    }
  }
}
