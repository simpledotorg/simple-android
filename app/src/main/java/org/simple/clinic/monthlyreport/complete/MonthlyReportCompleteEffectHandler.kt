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

class MonthlyReportCompleteEffectHandler @AssistedInject constructor(
    private val questionnaireResponseRepository: QuestionnaireResponseRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<MonthlyReportCompleteViewEffect>
) {
  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<MonthlyReportCompleteViewEffect>
    ): MonthlyReportCompleteEffectHandler
  }

  fun build(): ObservableTransformer<MonthlyReportCompleteEffect, MonthlyReportCompleteEvent> {
    return RxMobius
        .subtypeEffectHandler<MonthlyReportCompleteEffect, MonthlyReportCompleteEvent>()
        .addTransformer(LoadQuestionnaireResponseEffect::class.java, loadQuestionnaireResponse(schedulersProvider.io()))
        .addConsumer(MonthlyReportCompleteViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadQuestionnaireResponse(scheduler: Scheduler):
      ObservableTransformer<LoadQuestionnaireResponseEffect, MonthlyReportCompleteEvent> {
    return ObservableTransformer { loadQuestionnaireResponse ->
      loadQuestionnaireResponse
          .observeOn(scheduler)
          .map { questionnaireResponseRepository.questionnaireResponse(it.questionnaireId) }
          .map { QuestionnaireResponseFetched(it) }
    }
  }
}
