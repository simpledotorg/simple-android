package org.simple.clinic.bp.history

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bp.BloodPressureHistoryListItemPagingSource
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodPressureHistoryScreenEffectHandler @AssistedInject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    private val pagerFactory: PagerFactory,
    private val pagingSourceFactory: BloodPressureHistoryListItemPagingSource.Factory,
    private val patientSummaryConfig: PatientSummaryConfig,
    @Assisted private val viewEffectsConsumer: Consumer<BloodPressureHistoryViewEffect>,
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<BloodPressureHistoryViewEffect>,
    ): BloodPressureHistoryScreenEffectHandler
  }

  fun build(): ObservableTransformer<BloodPressureHistoryScreenEffect, BloodPressureHistoryScreenEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodPressureHistoryScreenEffect, BloodPressureHistoryScreenEvent>()
        .addTransformer(LoadPatient::class.java, loadPatient(schedulersProvider.io()))
        .addConsumer(BloodPressureHistoryViewEffect::class.java, viewEffectsConsumer::accept)
        .addTransformer(LoadBloodPressureHistory::class.java, loadBloodPressureHistory())
        .build()
  }

  private fun loadBloodPressureHistory(): ObservableTransformer<LoadBloodPressureHistory, BloodPressureHistoryScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap {
            val pagingSource = bloodPressureRepository.allBloodPressuresPagingSource(it.patientUuid)

            pagerFactory.createPager(
                sourceFactory = {
                  pagingSourceFactory.create(
                      bpEditableDuration = patientSummaryConfig.bpEditableDuration,
                      source = pagingSource,
                  )
                },
            )
          }
          .map(::BloodPressuresHistoryLoaded)
    }
  }

  private fun loadPatient(
      scheduler: Scheduler
  ): ObservableTransformer<LoadPatient, BloodPressureHistoryScreenEvent> {
    return ObservableTransformer { effect ->
      effect
          .switchMap {
            patientRepository
                .patient(it.patientUuid)
                .take(1)
                .subscribeOn(scheduler)
          }
          .filterAndUnwrapJust()
          .map(::PatientLoaded)
    }
  }
}
