package org.simple.clinic.home.patients.links

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.QuestionnaireRepository
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientsTabLinkEffectHandler @AssistedInject constructor(
    private val questionnaireRepository: QuestionnaireRepository,
    private val questionnaireResponseRepository: QuestionnaireResponseRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: PatientsTabLinkUiActions
) {
  @AssistedFactory
  interface Factory {
    fun create(uiActions: PatientsTabLinkUiActions): PatientsTabLinkEffectHandler
  }

  fun build(): ObservableTransformer<PatientsTabLinkEffect, PatientsTabLinkEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientsTabLinkEffect, PatientsTabLinkEvent>()
        .addTransformer(LoadMonthlyScreeningReportsFormEffect::class.java, loadMonthlyScreeningReportsFormEffect(schedulersProvider.io()))
        .addTransformer(LoadMonthlyScreeningReportsListEffect::class.java, loadMonthlyScreeningReportsListEffect(schedulersProvider.io()))
        .addAction(OpenMonthlyScreeningReportsListScreen::class.java, { uiActions.openMonthlyScreeningReports() }, schedulersProvider.ui())
        .addAction(OpenPatientLineListDownloadDialog::class.java, { uiActions.openPatientLineListDownloadDialog() }, schedulersProvider.ui())
        .build()
  }

  private fun loadMonthlyScreeningReportsFormEffect(scheduler: Scheduler):
      ObservableTransformer<LoadMonthlyScreeningReportsFormEffect, PatientsTabLinkEvent> {
    return ObservableTransformer { loadQuestionnaireForm ->
      loadQuestionnaireForm
          .observeOn(scheduler)
          .map { questionnaireRepository.questionnairesByType(MonthlyScreeningReports) }
          .map { MonthlyScreeningReportsFormFetched(it) }
    }
  }

  private fun loadMonthlyScreeningReportsListEffect(scheduler: Scheduler):
      ObservableTransformer<LoadMonthlyScreeningReportsListEffect, PatientsTabLinkEvent> {
    return ObservableTransformer { loadQuestionnaireResponse ->
      loadQuestionnaireResponse
          .observeOn(scheduler)
          .map { questionnaireResponseRepository.questionnaireResponsesByType(MonthlyScreeningReports) }
          .map { MonthlyScreeningReportsListFetched(it) }
    }
  }
}
