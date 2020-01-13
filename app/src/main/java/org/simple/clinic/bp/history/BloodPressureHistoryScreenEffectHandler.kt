package org.simple.clinic.bp.history

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class BloodPressureHistoryScreenEffectHandler @Inject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<BloodPressureHistoryScreenEffect, BloodPressureHistoryScreenEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodPressureHistoryScreenEffect, BloodPressureHistoryScreenEvent>()
        .addTransformer(LoadBloodPressureHistory::class.java, loadBloodPressureHistory(schedulersProvider.io()))
        .build()
  }

  private fun loadBloodPressureHistory(
      scheduler: Scheduler
  ): ObservableTransformer<LoadBloodPressureHistory, BloodPressureHistoryScreenEvent> {
    return ObservableTransformer { effect ->
      effect
          .switchMap {
            bloodPressureRepository
                .allBloodPressures(it.patientUuid)
                .subscribeOn(scheduler)
          }
          .map(::BloodPressureHistoryLoaded)
    }
  }
}
