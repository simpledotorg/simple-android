package org.simple.clinic.monthlyscreeningreports.list

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

class MonthlyScreeningReportListEffectHandler @AssistedInject constructor(
    private val currentFacility: Lazy<Facility>,
    private val questionnaireResponseRepository: QuestionnaireResponseRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<MonthlyScreeningReportListViewEffect>
) {
  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<MonthlyScreeningReportListViewEffect>
    ): MonthlyScreeningReportListEffectHandler
  }

  fun build(): ObservableTransformer<MonthlyScreeningReportListEffect, MonthlyScreeningReportListEvent> {
    return RxMobius
        .subtypeEffectHandler<MonthlyScreeningReportListEffect, MonthlyScreeningReportListEvent>()
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addTransformer(LoadMonthlyReportListEffect::class.java, loadQuestionnaireResponseList(schedulersProvider.io()))
        .addConsumer(MonthlyScreeningReportListViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadCurrentFacility(scheduler: Scheduler):
      ObservableTransformer<LoadCurrentFacility, MonthlyScreeningReportListEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadQuestionnaireResponseList(scheduler: Scheduler):
      ObservableTransformer<LoadMonthlyReportListEffect, MonthlyScreeningReportListEvent> {
    return ObservableTransformer { loadQuestionnaireResponseList ->
      loadQuestionnaireResponseList
          .observeOn(scheduler)
          .switchMap { questionnaireResponseRepository.questionnaireResponsesByType(MonthlyScreeningReports, currentFacility.get().uuid) }
          .map { MonthlyScreeningReportListFetched(it) }
    }
  }
}
