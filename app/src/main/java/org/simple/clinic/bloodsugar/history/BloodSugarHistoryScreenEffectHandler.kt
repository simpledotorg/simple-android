package org.simple.clinic.bloodsugar.history

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import kotlinx.coroutines.CoroutineScope
import org.simple.clinic.bloodsugar.BloodSugarHistoryListItemPagingSource
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfig
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodSugarHistoryScreenEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val bloodSugarRepository: BloodSugarRepository,
    private val schedulersProvider: SchedulersProvider,
    private val pagerFactory: PagerFactory,
    private val pagingSourceFactory: BloodSugarHistoryListItemPagingSource.Factory,
    private val config: BloodSugarSummaryConfig,
    private val bloodSugarUnitPreference: Preference<BloodSugarUnitPreference>,
    @Assisted private val viewEffectsConsumer: Consumer<BloodSugarHistoryScreenViewEffect>,
    @Assisted private val pagingCacheScope: () -> CoroutineScope
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<BloodSugarHistoryScreenViewEffect>,
        pagingCacheScope: () -> CoroutineScope,
    ): BloodSugarHistoryScreenEffectHandler
  }

  fun build(): ObservableTransformer<BloodSugarHistoryScreenEffect, BloodSugarHistoryScreenEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarHistoryScreenEffect, BloodSugarHistoryScreenEvent>()
        .addTransformer(LoadPatient::class.java, loadPatient(schedulersProvider.io()))
        .addConsumer(BloodSugarHistoryScreenViewEffect::class.java, viewEffectsConsumer::accept)
        .addTransformer(LoadBloodSugarHistory::class.java, loadBloodSugarHistory())
        .build()
  }

  private fun loadBloodSugarHistory(): ObservableTransformer<LoadBloodSugarHistory, BloodSugarHistoryScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap {
            val pagingSource = bloodSugarRepository.allBloodSugarsPagingSource(it.patientUuid)

            pagerFactory.createPager(
                sourceFactory = {
                  pagingSourceFactory.create(
                      canEditFor = config.bloodSugarEditableDuration,
                      bloodSugarUnitPreference = bloodSugarUnitPreference.get(),
                      source = pagingSource,
                  )
                },
                cacheScope = pagingCacheScope.invoke(),
            )
          }
          .map(::BloodSugarHistoryLoaded)
    }
  }

  private fun loadPatient(
      scheduler: Scheduler
  ): ObservableTransformer<LoadPatient, BloodSugarHistoryScreenEvent> {
    return ObservableTransformer { effects ->
      effects
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
