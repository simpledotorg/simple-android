package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class TeleconsultRecordEffectHandler @AssistedInject constructor(
    private val teleconsultRecordRepository: TeleconsultRecordRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): TeleconsultRecordEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultRecordEffect, TeleconsultRecordEvent> {
    return RxMobius.subtypeEffectHandler<TeleconsultRecordEffect, TeleconsultRecordEvent>()
        .addAction(GoBack::class.java, uiActions::goBackToPreviousScreen)
        .addConsumer(NavigateToTeleconsultSuccess::class.java, { uiActions.navigateToTeleconsultSuccessScreen(it.teleconsultRecordId) }, schedulersProvider.ui())
        .addTransformer(LoadTeleconsultRecordWithPrescribedDrugs::class.java, loadTeleconsultRecordWithPrescribedDrugs())
        .build()
  }

  private fun loadTeleconsultRecordWithPrescribedDrugs(): ObservableTransformer<LoadTeleconsultRecordWithPrescribedDrugs, TeleconsultRecordEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { teleconsultRecordRepository.getTeleconsultRecordWithPrescribedDrugs(it.teleconsultRecordId) }
          .map(::TeleconsultRecordWithPrescribedDrugsLoaded)
    }
  }
}
