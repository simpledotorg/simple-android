package org.simple.clinic.monthlyscreeningreports.form

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaire.QuestionnaireRepository
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.scheduler.SchedulersProvider

class QuestionnaireEntryEffectHandler @AssistedInject constructor(
    private val currentFacility: Lazy<Facility>,
    private val currentUser: Lazy<User>,
    private val questionnaireRepository: QuestionnaireRepository,
    private val questionnaireResponseRepository: QuestionnaireResponseRepository,
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
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addTransformer(LoadQuestionnaireFormEffect::class.java, loadQuestionnaireLayout(schedulersProvider.io()))
        .addTransformer(LoadQuestionnaireResponseEffect::class.java, loadQuestionnaireResponse(schedulersProvider.io()))
        .addTransformer(SaveQuestionnaireResponseEffect::class.java, saveQuestionnaireResponse(schedulersProvider.io()))
        .addConsumer(QuestionnaireEntryViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadCurrentFacility(scheduler: Scheduler):
      ObservableTransformer<LoadCurrentFacility, QuestionnaireEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadQuestionnaireLayout(scheduler: Scheduler):
      ObservableTransformer<LoadQuestionnaireFormEffect, QuestionnaireEntryEvent> {
    return ObservableTransformer { loadQuestionnaireForm ->
      loadQuestionnaireForm
          .observeOn(scheduler)
          .map { questionnaireRepository.questionnairesByTypeImmediate(it.questionnaireType) }
          .map { QuestionnaireFormFetched(it) }
    }
  }

  private fun loadQuestionnaireResponse(scheduler: Scheduler):
      ObservableTransformer<LoadQuestionnaireResponseEffect, QuestionnaireEntryEvent> {
    return ObservableTransformer { loadQuestionnaireResponse ->
      loadQuestionnaireResponse
          .observeOn(scheduler)
          .map { questionnaireResponseRepository.questionnaireResponse(it.questionnaireResponseId) }
          .map { QuestionnaireResponseFetched(it) }
    }
  }

  private fun saveQuestionnaireResponse(scheduler: Scheduler):
      ObservableTransformer<SaveQuestionnaireResponseEffect, QuestionnaireEntryEvent> {
    return ObservableTransformer { saveQuestionnaireResponse ->
      saveQuestionnaireResponse
          .observeOn(scheduler)
          .map {
            val user = currentUser.get()
            questionnaireResponseRepository.updateQuestionnaireResponse(user, it.questionnaireResponse)
          }
          .map { QuestionnaireResponseSaved }
    }
  }
}
