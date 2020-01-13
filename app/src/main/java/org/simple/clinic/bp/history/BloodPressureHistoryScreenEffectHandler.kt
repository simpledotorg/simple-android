package org.simple.clinic.bp.history

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodPressureHistoryScreenEffectHandler @AssistedInject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: BloodPressureHistoryScreenUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: BloodPressureHistoryScreenUiActions): BloodPressureHistoryScreenEffectHandler
  }

  fun build(): ObservableTransformer<BloodPressureHistoryScreenEffect, BloodPressureHistoryScreenEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodPressureHistoryScreenEffect, BloodPressureHistoryScreenEvent>()
        .addTransformer(LoadBloodPressureHistory::class.java, loadBloodPressureHistory(schedulersProvider.io()))
        .addAction(OpenBloodPressureEntrySheet::class.java, uiActions::openBloodPressureEntrySheet, schedulersProvider.ui())
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
