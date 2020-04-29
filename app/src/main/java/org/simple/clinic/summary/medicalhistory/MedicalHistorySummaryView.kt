package org.simple.clinic.summary.medicalhistory

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.medicalhistory_summary_view.view.*
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class MedicalHistorySummaryView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet), MedicalHistorySummaryUi {

  private val internalEvents = PublishSubject.create<MedicalHistorySummaryEvent>()

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controllerFactory: MedicalHistorySummaryUiController.Factory

  init {
    LayoutInflater.from(context).inflate(R.layout.medicalhistory_summary_view, this, true)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<MedicalHistorySummaryViewInjector>().inject(this)

    val screenKey = screenRouter.key<PatientSummaryScreenKey>(this)

    bindUiToController(
        ui = this,
        events = Observable.merge(screenCreates(), internalEvents),
        controller = controllerFactory.create(screenKey.patientUuid),
        screenDestroys = detaches().map { ScreenDestroyed() }
    )
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated())

  override fun populateMedicalHistory(medicalHistory: MedicalHistory) {
    renderMedicalHistory(medicalHistory)
    renderDiagnosis(medicalHistory)
  }

  private fun renderMedicalHistory(medicalHistory: MedicalHistory) {
    heartAttackQuestionView.render(HAS_HAD_A_HEART_ATTACK, medicalHistory.hasHadHeartAttack, ::answerToggled)
    strokeQuestionView.render(HAS_HAD_A_STROKE, medicalHistory.hasHadStroke, ::answerToggled)
    kidneyDiseaseQuestionView.render(HAS_HAD_A_KIDNEY_DISEASE, medicalHistory.hasHadKidneyDisease, ::answerToggled)
    diabetesQuestionView.render(DIAGNOSED_WITH_DIABETES, medicalHistory.diagnosedWithDiabetes, ::answerToggled)
  }

  private fun renderDiagnosis(medicalHistory: MedicalHistory) {
    hypertensionDiagnosisView.render(DIAGNOSED_WITH_HYPERTENSION, medicalHistory.diagnosedWithHypertension, ::answerToggled)
    diabetesDiagnosisView.render(DIAGNOSED_WITH_DIABETES, medicalHistory.diagnosedWithDiabetes, ::answerToggled)
  }

  override fun showDiagnosisView() {
    diagnosisViewContainer.visibility = VISIBLE
    diabetesDiagnosisView.hideDivider()
  }

  override fun hideDiagnosisView() {
    diagnosisViewContainer.visibility = GONE
  }

  override fun showDiabetesHistorySection() {
    diabetesQuestionView.visibility = VISIBLE
    kidneyDiseaseQuestionView.showDivider()
    diabetesQuestionView.hideDivider()
  }

  override fun hideDiabetesHistorySection() {
    diabetesQuestionView.visibility = GONE
    kidneyDiseaseQuestionView.hideDivider()
  }

  override fun hideDiagnosisError() {
    diagnosisRequiredError.visibility = GONE
  }

  fun showDiagnosisError() {
    diagnosisRequiredError.visibility = VISIBLE
  }

  private fun answerToggled(question: MedicalHistoryQuestion, answer: Answer) {
    internalEvents.onNext(SummaryMedicalHistoryAnswerToggled(question, answer))
  }
}
