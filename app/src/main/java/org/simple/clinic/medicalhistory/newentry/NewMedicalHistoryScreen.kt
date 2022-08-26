package org.simple.clinic.medicalhistory.newentry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.databinding.ScreenNewMedicalHistoryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithDiabetes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithHypertension
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAHeartAttack
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAKidneyDisease
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAStroke
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnDiabetesTreatment
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnHypertensionTreatment
import org.simple.clinic.medicalhistory.SelectDiagnosisErrorDialog
import org.simple.clinic.medicalhistory.SelectOngoingDiabetesTreatmentErrorDialog
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
    NewMedicalHistoryViewEffect>(), NewMedicalHistoryUi, NewMedicalHistoryUiActions {

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

  private val hotEvents: Subject<NewMedicalHistoryEvent> = PublishSubject.create()

  override fun defaultModel() = NewMedicalHistoryModel.default(country)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenNewMedicalHistoryBinding.inflate(layoutInflater, container, false)

  override fun createUpdate() = NewMedicalHistoryUpdate()

  override fun events() = Observable
      .merge(
          hotEvents,
          saveClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<NewMedicalHistoryEvent>()

  override fun createEffectHandler(
      viewEffectsConsumer: Consumer<NewMedicalHistoryViewEffect>
  ) = effectHandlerFactory
      .create(
          viewEffectsConsumer = viewEffectsConsumer
      )
      .build()

  override fun createInit() = NewMedicalHistoryInit()

  override fun uiRenderer() = NewMedicalHistoryUiRenderer(this)

  override fun viewEffectHandler() = NewMedicalHistoryViewEffectHandler(this)

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
      HasHadAHeartAttack -> heartAttackQuestionView
      HasHadAStroke -> strokeQuestionView
      HasHadAKidneyDisease -> kidneyDiseaseQuestionView
      DiagnosedWithDiabetes -> diabetesQuestionView
      else -> null
    }

    view?.render(question, answer) { questionForView, newAnswer ->
      hotEvents.onNext(NewMedicalHistoryAnswerToggled(questionForView, newAnswer))
    }
  }

  override fun showDiabetesDiagnosisView() {
    diabetesDiagnosis.visibility = VISIBLE
  }

  override fun hideDiabetesDiagnosisView() {
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
      DiagnosedWithHypertension -> hypertensionDiagnosis
      DiagnosedWithDiabetes -> diabetesDiagnosis
      else -> null
    }

    val label = when (question) {
      DiagnosedWithHypertension -> R.string.medicalhistory_diagnosis_hypertension_required
      DiagnosedWithDiabetes -> R.string.medicalhistory_diagnosis_diabetes_required
      else -> question.questionRes
    }

    view?.renderDiagnosis(label, question, answer) { questionForView, newAnswer ->
      hotEvents.onNext(NewMedicalHistoryAnswerToggled(questionForView, newAnswer))
    }
  }

  override fun showNextButtonProgress() {
    nextButton.setButtonState(InProgress)
  }

  override fun hideNextButtonProgress() {
    nextButton.setButtonState(Enabled)
  }

  override fun showHypertensionTreatmentQuestion(answer: Answer) {
    hypertensionDiagnosis.renderTreatmentQuestion(
        question = IsOnHypertensionTreatment(country.isoCountryCode),
        answer = answer
    ) { questionForView, newAnswer ->
      hotEvents.onNext(NewMedicalHistoryAnswerToggled(questionForView, newAnswer))
    }

    hypertensionDiagnosis.showTreatmentQuestion()
  }

  override fun hideHypertensionTreatmentQuestion() {
    hypertensionDiagnosis.hideTreatmentQuestion()
    hypertensionDiagnosis.clearTreatmentChipGroup()
  }

  override fun showDiabetesTreatmentQuestion(answer: Answer) {
    diabetesDiagnosis.renderTreatmentQuestion(IsOnDiabetesTreatment, answer) { questionForView, newAnswer ->
      hotEvents.onNext(NewMedicalHistoryAnswerToggled(questionForView, newAnswer))
    }

    diabetesDiagnosis.showTreatmentQuestion()
  }

  override fun hideDiabetesTreatmentQuestion() {
    diabetesDiagnosis.hideTreatmentQuestion()
    diabetesDiagnosis.clearTreatmentChipGroup()
  }

  override fun showOngoingHypertensionTreatmentErrorDialog() {
    SelectOngoingHypertensionTreatmentErrorDialog.show(fragmentManager = activity.supportFragmentManager)
  }

  override fun showOngoingDiabetesTreatmentErrorDialog() {
    SelectOngoingDiabetesTreatmentErrorDialog.show(fragmentManager = activity.supportFragmentManager)
  }

  override fun showDiagnosisRequiredErrorDialog() {
    SelectDiagnosisErrorDialog.show(activity.supportFragmentManager)
  }

  override fun showHypertensionDiagnosisRequiredErrorDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.select_diagnosis_error_diagnosis_required))
        .setMessage(getString(R.string.select_diagnosis_error_enter_diagnosis_hypertension))
        .setPositiveButton(getString(R.string.select_diagnosis_error_ok), null)
        .show()
  }

  override fun showChangeDiagnosisErrorDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.change_diagnosis_title))
        .setMessage(getString(R.string.change_diagnosis_message))
        .setPositiveButton(getString(R.string.change_diagnosis_positive), null)
        .setNegativeButton(getString(R.string.change_diagnosis_negative)) { _, _ ->
          hotEvents.onNext(ChangeDiagnosisNotNowClicked)
        }
        .show()
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
