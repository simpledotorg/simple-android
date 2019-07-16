package org.simple.clinic.medicalhistory.newentry

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestionView
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class NewMedicalHistoryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var controller: NewMedicalHistoryScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var utcClock: UtcClock

  private val toolbar by bindView<Toolbar>(R.id.newmedicalhistory_toolbar)
  private val diagnosedForHypertensionQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_diagnosed_for_hypertension)
  private val treatmentForHypertensionQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_treatment_for_hypertension)
  private val heartAttackQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_heartattack)
  private val strokeQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_stroke)
  private val kidneyDiseaseQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_kidney)
  private val diabetesQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_diabetes)
  private val nextButtonFrame by bindView<PrimarySolidButtonWithFrame>(R.id.newmedicalhistory_next_frame)

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
        events = Observable.merge(
            screenCreates(),
            answerToggles(),
            saveClicks()
        ),
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

  fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewNewPatient, Instant.now(utcClock)))
  }

  fun setPatientName(patientName: String) {
    toolbar.title = patientName
  }
}
