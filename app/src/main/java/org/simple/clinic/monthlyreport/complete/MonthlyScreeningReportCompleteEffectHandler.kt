package org.simple.clinic.monthlyreport.complete

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class MonthlyScreeningReportCompleteEffectHandler @AssistedInject constructor(
    private val questionnaireResponseRepository: QuestionnaireResponseRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<MonthlyScreeningReportCompleteViewEffect>
) {
  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<MonthlyScreeningReportCompleteViewEffect>
    ): MonthlyScreeningReportCompleteEffectHandler
  }

  fun build(): ObservableTransformer<MonthlyScreeningReportCompleteEffect, MonthlyScreeningReportCompleteEvent> {
    return RxMobius
        .subtypeEffectHandler<MonthlyScreeningReportCompleteEffect, MonthlyScreeningReportCompleteEvent>()
        .addTransformer(LoadQuestionnaireResponseEffect::class.java, loadQuestionnaireResponse(schedulersProvider.io()))
        .addConsumer(MonthlyScreeningReportCompleteViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadQuestionnaireResponse(scheduler: Scheduler):
      ObservableTransformer<LoadQuestionnaireResponseEffect, MonthlyScreeningReportCompleteEvent> {
    return ObservableTransformer { loadQuestionnaireResponse ->
      loadQuestionnaireResponse
          .observeOn(scheduler)
          .map { questionnaireResponseRepository.questionnaireResponse(it.questionnaireId) }
          .map { QuestionnaireResponseFetched(it) }
    }
  }
}
