package org.simple.clinic.medicalhistory.newentry

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.android.synthetic.main.screen_new_medical_history.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.main.TheActivity
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestionView_Old
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class NewMedicalHistoryScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), NewMedicalHistoryUi, NewMedicalHistoryUiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var effectHandlerFactory: NewMedicalHistoryEffectHandler.Factory

  private val events: Observable<NewMedicalHistoryEvent> by unsafeLazy {
    Observable
        .merge(answerToggles(), saveClicks())
        .compose(ReportAnalyticsEvents())
        .cast<NewMedicalHistoryEvent>()
  }

  private val uiRenderer: ViewRenderer<NewMedicalHistoryModel> = NewMedicalHistoryUiRenderer(this)

  private val mobiusDelegate: MobiusDelegate<NewMedicalHistoryModel, NewMedicalHistoryEvent, NewMedicalHistoryEffect> by unsafeLazy {
    MobiusDelegate(
        events = events,
        defaultModel = NewMedicalHistoryModel.default(),
        update = NewMedicalHistoryUpdate(),
        init = NewMedicalHistoryInit(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render,
        crashReporter = crashReporter
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    toolbar.setNavigationOnClickListener {
      screenRouter.pop()
    }

    diagnosedForHypertensionQuestionView.render(DIAGNOSED_WITH_HYPERTENSION, Unanswered)
    treatmentForHypertensionQuestionView.render(IS_ON_TREATMENT_FOR_HYPERTENSION, Unanswered)
    heartAttackQuestionView.render(HAS_HAD_A_HEART_ATTACK, Unanswered)
    strokeQuestionView.render(HAS_HAD_A_STROKE, Unanswered)
    kidneyDiseaseQuestionView.render(HAS_HAD_A_KIDNEY_DISEASE, Unanswered)
    diabetesQuestionView.render(HAS_DIABETES, Unanswered)

    diabetesQuestionView.hideDivider()

    mobiusDelegate.prepare()

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
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return mobiusDelegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(mobiusDelegate.onRestoreInstanceState(state))
  }

  private fun answerToggles(): Observable<UiEvent> {
    val toggles = { view: MedicalHistoryQuestionView_Old ->
      view.answers().map { answer -> NewMedicalHistoryAnswerToggled(view.question, answer) }
    }

    return Observable.mergeArray(
        toggles(diagnosedForHypertensionQuestionView),
        toggles(treatmentForHypertensionQuestionView),
        toggles(heartAttackQuestionView),
        toggles(strokeQuestionView),
        toggles(kidneyDiseaseQuestionView),
        toggles(diabetesQuestionView))
  }

  private fun saveClicks() =
      nextButtonFrame.button
          .clicks()
          .map { SaveMedicalHistoryClicked() }

  override fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewNewPatient, Instant.now(utcClock)))
  }

  override fun setPatientName(patientName: String) {
    toolbar.title = patientName
  }

  override fun renderAnswerForQuestion(question: MedicalHistoryQuestion, answer: Answer) {
    val view = when (question) {
      DIAGNOSED_WITH_HYPERTENSION -> diagnosedForHypertensionQuestionView
      IS_ON_TREATMENT_FOR_HYPERTENSION -> treatmentForHypertensionQuestionView
      HAS_HAD_A_HEART_ATTACK -> heartAttackQuestionView
      HAS_HAD_A_STROKE -> strokeQuestionView
      HAS_HAD_A_KIDNEY_DISEASE -> kidneyDiseaseQuestionView
      HAS_DIABETES -> diabetesQuestionView
    }

    view.render(question, answer)
  }
}
