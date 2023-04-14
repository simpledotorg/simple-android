package org.simple.clinic.monthlyreport.list

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class MonthlyReportListEffectHandler @AssistedInject constructor(
    private val currentFacility: Lazy<Facility>,
    private val questionnaireResponseRepository: QuestionnaireResponseRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<MonthlyReportListViewEffect>
) {
  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<MonthlyReportListViewEffect>
    ): MonthlyReportListEffectHandler
  }

  fun build(): ObservableTransformer<MonthlyReportListEffect, MonthlyReportListEvent> {
    return RxMobius
        .subtypeEffectHandler<MonthlyReportListEffect, MonthlyReportListEvent>()
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addTransformer(LoadMonthlyReportListEffect::class.java, loadQuestionnaireResponseList(schedulersProvider.io()))
        .addConsumer(MonthlyReportListViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadCurrentFacility(scheduler: Scheduler):
      ObservableTransformer<LoadCurrentFacility, MonthlyReportListEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadQuestionnaireResponseList(scheduler: Scheduler):
      ObservableTransformer<LoadMonthlyReportListEffect, MonthlyReportListEvent> {
    return ObservableTransformer { loadQuestionnaireResponseList ->
      loadQuestionnaireResponseList
          .observeOn(scheduler)
          .switchMap { questionnaireResponseRepository.questionnaireResponsesFilteredBy(MonthlyScreeningReports, currentFacility.get().uuid) }
          .map { MonthlyReportListFetched(it) }
    }
  }
}
