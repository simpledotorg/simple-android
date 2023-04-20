package org.simple.clinic.monthlyreports.list

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class MonthlyReportsEffectHandler @AssistedInject constructor(
    private val currentFacility: Lazy<Facility>,
    private val questionnaireResponseRepository: QuestionnaireResponseRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<MonthlyReportsViewEffect>
) {
  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<MonthlyReportsViewEffect>
    ): MonthlyReportsEffectHandler
  }

  fun build(): ObservableTransformer<MonthlyReportsEffect, MonthlyReportsEvent> {
    return RxMobius
        .subtypeEffectHandler<MonthlyReportsEffect, MonthlyReportsEvent>()
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addTransformer(LoadMonthlyReportsEffect::class.java, loadMonthlyReports(schedulersProvider.io()))
        .addConsumer(MonthlyReportsViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadCurrentFacility(scheduler: Scheduler):
      ObservableTransformer<LoadCurrentFacility, MonthlyReportsEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadMonthlyReports(scheduler: Scheduler):
      ObservableTransformer<LoadMonthlyReportsEffect, MonthlyReportsEvent> {
    return ObservableTransformer { loadQuestionnaireResponseList ->
      loadQuestionnaireResponseList
          .observeOn(scheduler)
          .switchMap {
            questionnaireResponseRepository.questionnaireResponsesFilteredBy(it.questionnaireType, currentFacility.get().uuid)
          }
          .map { MonthlyReportsFetched(it) }
    }
  }
}
