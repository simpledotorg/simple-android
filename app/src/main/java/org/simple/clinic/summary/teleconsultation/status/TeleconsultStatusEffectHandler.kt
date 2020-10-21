package org.simple.clinic.summary.teleconsultation.status

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class TeleconsultStatusEffectHandler @Inject constructor(
    private val teleconsultRecordRepository: TeleconsultRecordRepository,
    private val schedulersProvider: SchedulersProvider,
) {

  fun build(): ObservableTransformer<TeleconsultStatusEffect, TeleconsultStatusEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultStatusEffect, TeleconsultStatusEvent>()
        .addTransformer(UpdateTeleconsultStatus::class.java, updateTeleconsultStatus())
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
