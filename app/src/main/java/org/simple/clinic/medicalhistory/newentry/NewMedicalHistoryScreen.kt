package org.simple.clinic.medicalhistory.newentry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.databinding.ScreenNewMedicalHistoryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_HYPERTENSION_TREATMENT
import org.simple.clinic.medicalhistory.SelectDiagnosisErrorDialog
import org.simple.clinic.medicalhistory.SelectOngoingHypertensionTreatmentErrorDialog
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class NewMedicalHistoryScreen : BaseScreen<
    NewMedicalHistoryScreen.Key,
    ScreenNewMedicalHistoryBinding,
    NewMedicalHistoryModel,
    NewMedicalHistoryEvent,
    NewMedicalHistoryEffect,
    Unit>(), NewMedicalHistoryUi, NewMedicalHistoryUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerFactory: NewMedicalHistoryEffectHandler.Factory

  @Inject
  lateinit var country: Country

  private val toolbar
    get() = binding.toolbar

  private val nextButton
    get() = binding.nextButton

  private val heartAttackQuestionView
    get() = binding.heartAttackQuestionView

  private val strokeQuestionView
    get() = binding.strokeQuestionView

  private val kidneyDiseaseQuestionView
    get() = binding.kidneyDiseaseQuestionView

  private val diabetesQuestionView
    get() = binding.diabetesQuestionView

  private val scrollView
    get() = binding.scrollView

  private val hypertensionDiagnosis
    get() = binding.hypertensionDiagnosis

  private val diabetesDiagnosis
    get() = binding.diabetesDiagnosis

  private val questionViewEvents: Subject<NewMedicalHistoryEvent> = PublishSubject.create()

  override fun defaultModel() = NewMedicalHistoryModel.default(country)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenNewMedicalHistoryBinding.inflate(layoutInflater, container, false)

  override fun createUpdate() = NewMedicalHistoryUpdate()

  override fun events() = Observable
      .merge(
          questionViewEvents,
          saveClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<NewMedicalHistoryEvent>()

  override fun createEffectHandler(
      viewEffectsConsumer: Consumer<Unit>
  ) = effectHandlerFactory
      .create(this)
      .build()

  override fun createInit() = NewMedicalHistoryInit()

  override fun uiRenderer() = NewMedicalHistoryUiRenderer(this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setNavigationOnClickListener {
      router.pop()
    }
  }

  private fun saveClicks() = nextButton
      .clicks()
      .map { SaveMedicalHistoryClicked() }

  override fun openPatientSummaryScreen(patientUuid: UUID) {
    router.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewNewPatient, Instant.now(utcClock)))
  }

  override fun setPatientName(patientName: String) {
    toolbar.title = patientName
  }

  override fun renderAnswerForQuestion(question: MedicalHistoryQuestion, answer: Answer) {
    val view = when (question) {
      HAS_HAD_A_HEART_ATTACK -> heartAttackQuestionView
      HAS_HAD_A_STROKE -> strokeQuestionView
      HAS_HAD_A_KIDNEY_DISEASE -> kidneyDiseaseQuestionView
      DIAGNOSED_WITH_DIABETES -> diabetesQuestionView
      else -> null
    }

    view?.render(question, answer) { questionForView, newAnswer ->
      questionViewEvents.onNext(NewMedicalHistoryAnswerToggled(questionForView, newAnswer))
    }
  }

  override fun showDiagnosisView() {
    // TODO: Rename this to show diabetes diagnosis view
    diabetesDiagnosis.visibility = VISIBLE
  }

  override fun hideDiagnosisView() {
    // TODO: Rename this to hide diabetes diagnosis view
    diabetesDiagnosis.visibility = GONE
  }

  override fun hideDiabetesHistorySection() {
    diabetesQuestionView.visibility = GONE
    kidneyDiseaseQuestionView.hideDivider()
  }

  override fun showDiabetesHistorySection() {
    diabetesQuestionView.visibility = VISIBLE
    kidneyDiseaseQuestionView.showDivider()
    diabetesQuestionView.hideDivider()
  }

  override fun renderDiagnosisAnswer(question: MedicalHistoryQuestion, answer: Answer) {
    val view = when (question) {
      DIAGNOSED_WITH_HYPERTENSION -> hypertensionDiagnosis
      DIAGNOSED_WITH_DIABETES -> diabetesDiagnosis
      else -> null
    }

    view?.renderDiagnosis(question, answer) { questionForView, newAnswer ->
      questionViewEvents.onNext(NewMedicalHistoryAnswerToggled(questionForView, newAnswer))
    }
  }

  override fun showNextButtonProgress() {
    nextButton.setButtonState(InProgress)
  }

  override fun hideNextButtonProgress() {
    nextButton.setButtonState(Enabled)
  }

  override fun showHypertensionTreatmentQuestion(answer: Answer) {
    hypertensionDiagnosis.renderTreatmentQuestion(IS_ON_HYPERTENSION_TREATMENT, answer) { questionForView, newAnswer ->
      questionViewEvents.onNext(NewMedicalHistoryAnswerToggled(questionForView, newAnswer))
    }
    TransitionManager.beginDelayedTransition(scrollView, ChangeBounds())

    hypertensionDiagnosis.showTreatmentQuestion()
  }

  override fun hideHypertensionTreatmentQuestion() {
    hypertensionDiagnosis.hideTreatmentQuestion()
    hypertensionDiagnosis.clearTreatmentChipGroup()
  }

  override fun showOngoingHypertensionTreatmentErrorDialog() {
    SelectOngoingHypertensionTreatmentErrorDialog.show(fragmentManager = activity.supportFragmentManager)
  }

  override fun showDiagnosisRequiredErrorDialog() {
    SelectDiagnosisErrorDialog.show(activity.supportFragmentManager)
  }

  interface Injector {
    fun inject(target: NewMedicalHistoryScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "New Medical History Entry"
  ) : ScreenKey() {
    override fun instantiateFragment() = NewMedicalHistoryScreen()
  }
}
