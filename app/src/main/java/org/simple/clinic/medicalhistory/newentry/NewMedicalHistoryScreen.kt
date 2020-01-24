package org.simple.clinic.medicalhistory.newentry

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_new_medical_history.view.*
import org.simple.clinic.bindUiToController
import org.simple.clinic.main.TheActivity
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestionView
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class NewMedicalHistoryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), NewMedicalHistoryUi {

  @Inject
  lateinit var controller: NewMedicalHistoryScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var crashReporter: CrashReporter

  private val events: Observable<UiEvent> by unsafeLazy {
    Observable.merge(
        screenCreates(),
        answerToggles(),
        saveClicks()
    )
  }

  private val uiRenderer: ViewRenderer<NewMedicalHistoryModel> = NewMedicalHistoryUiRenderer(this)

  private val mobiusDelegate: MobiusDelegate<NewMedicalHistoryModel, NewMedicalHistoryEvent, NewMedicalHistoryEffect> by unsafeLazy {
    MobiusDelegate(
        events = events.ofType(),
        defaultModel = NewMedicalHistoryModel(),
        update = NewMedicalHistoryUpdate(),
        init = NewMedicalHistoryInit(),
        effectHandler = NewMedicalHistoryEffectHandler().build(),
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

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    post {
      hideKeyboard()
    }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun answerToggles(): Observable<UiEvent> {
    val toggles = { view: MedicalHistoryQuestionView ->
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
      RxView
          .clicks(nextButtonFrame.button)
          .map { SaveMedicalHistoryClicked() }

  override fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewNewPatient, Instant.now(utcClock)))
  }

  override fun setPatientName(patientName: String) {
    toolbar.title = patientName
  }
}
