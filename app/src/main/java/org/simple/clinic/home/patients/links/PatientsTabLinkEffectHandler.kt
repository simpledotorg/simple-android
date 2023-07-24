package org.simple.clinic.home.patients.links

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.facility.Facility
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.MonthlySuppliesReports
import org.simple.clinic.questionnaire.QuestionnaireRepository
import org.simple.clinic.questionnaire.QuestionnaireResponseSections
import org.simple.clinic.questionnaire.QuestionnaireSections
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientsTabLinkEffectHandler @AssistedInject constructor(
    private val currentFacility: Observable<Facility>,
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
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addTransformer(LoadQuestionnaires::class.java, loadQuestionnaires(schedulersProvider.io()))
        .addTransformer(LoadQuestionnaireResponses::class.java, loadQuestionnaireResponses(schedulersProvider.io()))
        .addAction(OpenMonthlyScreeningReportsListScreen::class.java, { uiActions.openMonthlyScreeningReports() }, schedulersProvider.ui())
        .addAction(OpenMonthlySuppliesReportsListScreen::class.java, { uiActions.openMonthlySuppliesReports() }, schedulersProvider.ui())
        .addAction(OpenPatientLineListDownloadDialog::class.java, { uiActions.openPatientLineListDownloadDialog() }, schedulersProvider.ui())
        .addAction(OpenDrugStockReportsScreen::class.java, { uiActions.openDrugStockReports() }, schedulersProvider.ui())
        .build()
  }

  private fun loadCurrentFacility(scheduler: Scheduler):
      ObservableTransformer<LoadCurrentFacility, PatientsTabLinkEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .switchMap { currentFacility }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadQuestionnaires(scheduler: Scheduler):
      ObservableTransformer<LoadQuestionnaires, PatientsTabLinkEvent> {

    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .switchMap { questionnaireRepository.questionnaires() }
          .map { questionnaires ->
            val questionnaireSections = QuestionnaireSections(
                screeningQuestionnaire = questionnaires.firstOrNull { it.questionnaire_type == MonthlyScreeningReports },
                suppliesQuestionnaire = questionnaires.firstOrNull { it.questionnaire_type == MonthlySuppliesReports }
            )
            QuestionnairesLoaded(questionnaireSections = questionnaireSections)
          }
    }
  }

  private fun loadQuestionnaireResponses(scheduler: Scheduler):
      ObservableTransformer<LoadQuestionnaireResponses, PatientsTabLinkEvent> {

    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .switchMap { currentFacility }
          .switchMap { facility ->
            questionnaireResponseRepository.questionnaireResponsesInFacility(facility.uuid)
          }
          .map { questionnaireResponseList ->
            val questionnaireResponseSections = QuestionnaireResponseSections(
                screeningQuestionnaireResponseList = questionnaireResponseList.filter {
                  it.questionnaireType == MonthlyScreeningReports
                },
                suppliesQuestionnaireResponseList = questionnaireResponseList.filter {
                  it.questionnaireType == MonthlySuppliesReports
                },
            )
            QuestionnaireResponsesLoaded(questionnaireResponseSections)
          }
    }
  }
}
