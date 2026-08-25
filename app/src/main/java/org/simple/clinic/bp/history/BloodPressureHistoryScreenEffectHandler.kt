package org.simple.clinic.bp.history

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import kotlinx.coroutines.CoroutineScope
import org.simple.clinic.appconfig.Country
import org.simple.clinic.bp.BloodPressureHistoryListItemPagingSource
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator

class BloodPressureHistoryScreenEffectHandler @AssistedInject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    private val pagerFactory: PagerFactory,
    private val pagingSourceFactory: BloodPressureHistoryListItemPagingSource.Factory,
    private val patientSummaryConfig: PatientSummaryConfig,
    private val uuidGenerator: UuidGenerator,
    @Assisted private val viewEffectsConsumer: Consumer<BloodPressureHistoryViewEffect>,
    @Assisted private val pagingCacheScope: () -> CoroutineScope
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<BloodPressureHistoryViewEffect>,
        pagingCacheScope: () -> CoroutineScope
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

  private fun loadBloodPressureHistory():
      ObservableTransformer<LoadBloodPressureHistory, BloodPressureHistoryScreenEvent> {

    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { effect ->
            val pagingSource =
                bloodPressureRepository.allBloodPressuresPagingSource(
                    effect.patientUuid
                )

            medicalHistoryRepository
                .historyForPatientOrDefault(
                    defaultHistoryUuid = uuidGenerator.v4(),
                    patientUuid = effect.patientUuid
                )
                .flatMap { history ->

                  pagerFactory.createPager(
                      sourceFactory = {
                        pagingSourceFactory.create(
                            bpEditableDuration = patientSummaryConfig.bpEditableDuration,
                            source = pagingSource,
                            hasDiabetes = history.diagnosedWithDiabetes == Answer.Yes
                        )
                      },
                      cacheScope = pagingCacheScope.invoke(),
                  )
                }
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
          .extractIfPresent()
          .map(::PatientLoaded)
    }
  }
}
