package org.simple.clinic.medicalhistory.newentry

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.NONE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestionView
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.PatientSummaryCaller
import org.simple.clinic.summary.PatientSummaryScreen
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class NewMedicalHistoryScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = NewMedicalHistoryScreenKey()
  }

  @Inject
  lateinit var controller: NewMedicalHistoryScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val toolbar by bindView<Toolbar>(R.id.newmedicalhistory_toolbar)
  private val heartAttackQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_heartattack)
  private val strokeQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_stroke)
  private val kidneyDiseaseQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_kidney)
  private val hypertensionQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_hypertension)
  private val diabetesQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_diabetes)
  private val noneQuestionView by bindView<MedicalHistoryQuestionView>(R.id.newmedicalhistory_question_none)
  private val nextButtonFrame by bindView<PrimarySolidButtonWithFrame>(R.id.newmedicalhistory_next_frame)
  private val nextButton by bindView<Button>(R.id.newmedicalhistory_save)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    toolbar.setNavigationOnClickListener {
      screenRouter.pop()
    }

    heartAttackQuestionView.render(HAS_HAD_A_HEART_ATTACK)
    strokeQuestionView.render(HAS_HAD_A_STROKE)
    kidneyDiseaseQuestionView.render(HAS_HAD_A_KIDNEY_DISEASE)
    hypertensionQuestionView.render(IS_ON_TREATMENT_FOR_HYPERTENSION)
    diabetesQuestionView.render(HAS_DIABETES)
    noneQuestionView.render(NONE)

    Observable.mergeArray(screenCreates(), answerToggles(), saveClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { it(this) }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun answerToggles(): Observable<UiEvent> {
    val toggles = { view: MedicalHistoryQuestionView ->
      view.checkedChanges()
          .map { NewMedicalHistoryAnswerToggled(view.question, view.isChecked) }
    }

    return Observable.mergeArray(
        toggles(heartAttackQuestionView),
        toggles(strokeQuestionView),
        toggles(kidneyDiseaseQuestionView),
        toggles(hypertensionQuestionView),
        toggles(diabetesQuestionView),
        toggles(noneQuestionView))
  }

  private fun saveClicks() =
      RxView
          .clicks(nextButtonFrame.button)
          .map { SaveMedicalHistoryClicked() }

  fun unSelectNoneAnswer() {
    noneQuestionView.isChecked = false
  }

  fun unSelectAllAnswersExceptNone() {
    heartAttackQuestionView.isChecked = false
    strokeQuestionView.isChecked = false
    kidneyDiseaseQuestionView.isChecked = false
    hypertensionQuestionView.isChecked = false
    diabetesQuestionView.isChecked = false
  }

  fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreen.KEY(patientUuid, PatientSummaryCaller.NEW_PATIENT))
  }

  fun setSaveButtonEnabled(enabled: Boolean) {
    nextButtonFrame.isEnabled = enabled
    nextButton.isEnabled = enabled
  }
}
