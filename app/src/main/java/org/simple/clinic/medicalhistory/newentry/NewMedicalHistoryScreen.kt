package org.simple.clinic.medicalhistory.newentry

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFade
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.databinding.ListMedicalhistoryHypertensionTreatmentBinding
import org.simple.clinic.databinding.ScreenNewMedicalHistoryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_HYPERTENSION_TREATMENT
import org.simple.clinic.medicalhistory.SelectDiagnosisErrorDialog
import org.simple.clinic.medicalhistory.SelectOngoingHypertensionTreatmentErrorDialog
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import org.simple.clinic.widgets.hideKeyboard
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class NewMedicalHistoryScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), NewMedicalHistoryUi, NewMedicalHistoryUiActions {

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

  private var binding: ScreenNewMedicalHistoryBinding? = null
  private var hypertensionTreatmentBinding: ListMedicalhistoryHypertensionTreatmentBinding? = null

  private val toolbar
    get() = binding!!.toolbar

  private val nextButton
    get() = binding!!.nextButton

  private val heartAttackQuestionView
    get() = binding!!.heartAttackQuestionView

  private val strokeQuestionView
    get() = binding!!.strokeQuestionView

  private val kidneyDiseaseQuestionView
    get() = binding!!.kidneyDiseaseQuestionView

  private val diabetesQuestionView
    get() = binding!!.diabetesQuestionView

  private val diagnosisViewContainer
    get() = binding!!.diagnosisViewContainer

  private val diabetesDiagnosisView
    get() = binding!!.diabetesDiagnosisView

  private val hypertensionDiagnosisView
    get() = binding!!.hypertensionDiagnosisView

  private val hypertensionTreatmentContainer
    get() = binding!!.hypertensionTreatmentContainer

  private val scrollView
    get() = binding!!.scrollView

  private val hypertensionTreatmentChipGroup
    get() = hypertensionTreatmentBinding!!.chipGroup

  private val hypertensionTreatmentYesChip
    get() = hypertensionTreatmentBinding!!.yesChip

  private val hypertensionTreatmentNoChip
    get() = hypertensionTreatmentBinding!!.noChip

  private val questionViewEvents: Subject<NewMedicalHistoryEvent> = PublishSubject.create()

  private val events: Observable<NewMedicalHistoryEvent> by unsafeLazy {
    Observable
        .merge(questionViewEvents, saveClicks())
        .compose(ReportAnalyticsEvents())
        .cast<NewMedicalHistoryEvent>()
  }

  private val mobiusDelegate: MobiusDelegate<NewMedicalHistoryModel, NewMedicalHistoryEvent, NewMedicalHistoryEffect> by unsafeLazy {
    val uiRenderer: ViewRenderer<NewMedicalHistoryModel> = NewMedicalHistoryUiRenderer(this)

    MobiusDelegate.forView(
        events = events,
        defaultModel = NewMedicalHistoryModel.default(country),
        update = NewMedicalHistoryUpdate(),
        init = NewMedicalHistoryInit(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  private val hypertensionContainerFade by unsafeLazy {
    MaterialFade().apply {
      duration = 150L
      addTarget(hypertensionTreatmentContainer)
    }
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenNewMedicalHistoryBinding.bind(this)
    hypertensionTreatmentBinding = ListMedicalhistoryHypertensionTreatmentBinding.bind(binding!!.hypertensionTreatmentContainer)

    context.injector<Injector>().inject(this)

    toolbar.setNavigationOnClickListener {
      router.pop()
    }

    post {
      hideKeyboard()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    mobiusDelegate.start()
  }

  override fun onDetachedFromWindow() {
    mobiusDelegate.stop()
    binding = null
    hypertensionTreatmentBinding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return mobiusDelegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(mobiusDelegate.onRestoreInstanceState(state))
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
    diagnosisViewContainer.visibility = VISIBLE
    diabetesDiagnosisView.hideDivider()
  }

  override fun hideDiagnosisView() {
    diagnosisViewContainer.visibility = GONE
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
      DIAGNOSED_WITH_HYPERTENSION -> hypertensionDiagnosisView
      DIAGNOSED_WITH_DIABETES -> diabetesDiagnosisView
      else -> null
    }

    view?.render(question, answer) { questionForView, newAnswer ->
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
    hypertensionTreatmentChipGroup.setOnCheckedChangeListener(null)

    TransitionManager.beginDelayedTransition(scrollView, hypertensionContainerFade)

    hypertensionTreatmentContainer.visibility = View.VISIBLE
    hypertensionTreatmentYesChip.isChecked = answer == Yes
    hypertensionTreatmentNoChip.isChecked = answer == No

    hypertensionTreatmentChipGroup.setOnCheckedChangeListener { _, checkedId ->
      val checkedAnswer = when (checkedId) {
        R.id.yesChip -> Yes
        R.id.noChip -> No
        else -> Answer.Unanswered
      }
      questionViewEvents.onNext(NewMedicalHistoryAnswerToggled(IS_ON_HYPERTENSION_TREATMENT, checkedAnswer))
    }
  }

  override fun hideHypertensionTreatmentQuestion() {
    TransitionManager.beginDelayedTransition(scrollView, hypertensionContainerFade)

    hypertensionTreatmentContainer.visibility = View.GONE
    hypertensionTreatmentChipGroup.clearCheck()
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
}
