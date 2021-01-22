package org.simple.clinic.summary.medicalhistory

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.MedicalhistorySummaryViewBinding
import org.simple.clinic.di.injector
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.SelectDiagnosisErrorDialog
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.summary.PatientSummaryChildView
import org.simple.clinic.summary.PatientSummaryModelUpdateCallback
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class MedicalHistorySummaryView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet), MedicalHistorySummaryUi, PatientSummaryChildView {

  private var binding: MedicalhistorySummaryViewBinding? = null

  private val heartAttackQuestionView
    get() = binding!!.heartAttackQuestionView

  private val strokeQuestionView
    get() = binding!!.strokeQuestionView

  private val kidneyDiseaseQuestionView
    get() = binding!!.kidneyDiseaseQuestionView

  private val diabetesQuestionView
    get() = binding!!.diabetesQuestionView

  private val hypertensionDiagnosisView
    get() = binding!!.hypertensionDiagnosisView

  private val diabetesDiagnosisView
    get() = binding!!.diabetesDiagnosisView

  private val diagnosisViewContainer
    get() = binding!!.diagnosisViewContainer

  private val internalEvents = PublishSubject.create<MedicalHistorySummaryEvent>()

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandler: MedicalHistorySummaryEffectHandler

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  init {
    val layoutInflater = LayoutInflater.from(context)
    binding = MedicalhistorySummaryViewBinding.inflate(layoutInflater, this, true)
  }

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  private val screenKey by unsafeLazy { screenKeyProvider.keyFor<PatientSummaryScreenKey>(this) }

  private val events by unsafeLazy {
    Observable
        .merge(
            screenCreates(),
            internalEvents
        )
        .compose(ReportAnalyticsEvents())
  }

  private val uiRenderer = MedicalHistorySummaryUiRenderer(this)

  private val delegate by unsafeLazy {
    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = MedicalHistorySummaryModel.create(screenKey.patientUuid),
        update = MedicalHistorySummaryUpdate(),
        init = MedicalHistorySummaryInit(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = { model ->
          modelUpdateCallback?.invoke(model)
          uiRenderer.render(model)
        }
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<MedicalHistorySummaryViewInjector>().inject(this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
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

  override fun registerSummaryModelUpdateCallback(callback: PatientSummaryModelUpdateCallback?) {
    modelUpdateCallback = callback
  }

  fun showDiagnosisError() {
    SelectDiagnosisErrorDialog.show(activity.supportFragmentManager)
  }

  private fun answerToggled(question: MedicalHistoryQuestion, answer: Answer) {
    internalEvents.onNext(SummaryMedicalHistoryAnswerToggled(question, answer))
  }
}
