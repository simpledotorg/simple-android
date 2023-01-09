package org.simple.clinic.monthlyReports.questionnaire.entry

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class QuestionnaireEntryEffectHandler @AssistedInject constructor(
    private val questionnaireRepository: QuestionnaireRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<QuestionnaireEntryViewEffect>
) {
  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<QuestionnaireEntryViewEffect>
    ): QuestionnaireEntryEffectHandler
  }

  fun build(): ObservableTransformer<QuestionnaireEntryEffect, QuestionnaireEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<QuestionnaireEntryEffect, QuestionnaireEntryEvent>()
        .addTransformer(LoadQuestionnaireFormEffect::class.java, loadQuestionnaireLayout(schedulersProvider.io()))
        .addConsumer(QuestionnaireEntryViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadQuestionnaireLayout(scheduler: Scheduler): ObservableTransformer<LoadQuestionnaireFormEffect, QuestionnaireEntryEvent> {
    return ObservableTransformer { loadQuestionnaireForm ->
      loadQuestionnaireForm
          .observeOn(scheduler)
          .map { questionnaireRepository.questionnairesByType(it.questionnaireType) }
          .map { QuestionnaireFormFetched(it) }
    }
  }
}
