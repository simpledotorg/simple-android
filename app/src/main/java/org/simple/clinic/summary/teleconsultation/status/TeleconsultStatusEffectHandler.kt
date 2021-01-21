package org.simple.clinic.summary.teleconsultation.status

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class TeleconsultStatusEffectHandler @AssistedInject constructor(
    private val teleconsultRecordRepository: TeleconsultRecordRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: TeleconsultStatusUiAction
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: TeleconsultStatusUiAction): TeleconsultStatusEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultStatusEffect, TeleconsultStatusEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultStatusEffect, TeleconsultStatusEvent>()
        .addTransformer(UpdateTeleconsultStatus::class.java, updateTeleconsultStatus())
        .addAction(CloseSheet::class.java, uiActions::dismissSheet, schedulersProvider.ui())
        .build()
  }

  private fun updateTeleconsultStatus(): ObservableTransformer<UpdateTeleconsultStatus, TeleconsultStatusEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { teleconsultRecordRepository.updateRequesterCompletionStatus(it.teleconsultRecordId, it.teleconsultStatus) }
          .map { TeleconsultStatusUpdated }
    }
  }
}
