package org.simple.clinic.summary.medicalhistory

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.SelectDiagnosisErrorDialog
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.summary.PatientSummaryChildView
import org.simple.clinic.summary.PatientSummaryModelUpdateCallback
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.summary.medicalhistory.ui.MedicalHistorySummary
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class MedicalHistorySummaryView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet), MedicalHistorySummaryUi, PatientSummaryChildView {

  private val internalEvents = PublishSubject.create<MedicalHistorySummaryEvent>()

  private var medicalHistory by mutableStateOf<MedicalHistory?>(null)
  private var diabetesManagementEnabled by mutableStateOf(false)
  private var showSmokerQuestion by mutableStateOf(false)

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandler: MedicalHistorySummaryEffectHandler

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  @Inject
  lateinit var features: Features

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
        defaultModel = MedicalHistorySummaryModel.create(
            patientUuid = screenKey.patientUuid,
            showIsSmokingQuestion = features.isEnabled(Feature.NonLabBasedStatinNudge) ||
                features.isEnabled(Feature.LabBasedStatinNudge)
        ),
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

    addView(ComposeView(context).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

      setContent {
        SimpleTheme {
          AnimatedVisibility(
              visible = medicalHistory != null
          ) {
            MedicalHistorySummary(
                hypertensionAnswer = medicalHistory?.diagnosedWithHypertension,
                diabetesAnswer = medicalHistory?.diagnosedWithDiabetes,
                heartAttackAnswer = medicalHistory?.hasHadHeartAttack,
                strokeAnswer = medicalHistory?.hasHadStroke,
                kidneyAnswer = medicalHistory?.hasHadKidneyDisease,
                smokerAnswer = medicalHistory?.isSmoking,
                diabetesManagementEnabled = diabetesManagementEnabled,
                showSmokerQuestion = showSmokerQuestion,
            ) { question, answer ->
              answerToggled(question, answer)
            }
          }
        }
      }
    })
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun screenCreates(): Observable<UiEvent> = Observable.just(ScreenCreated)

  override fun populateMedicalHistory(medicalHistory: MedicalHistory) {
    this.medicalHistory = medicalHistory
  }

  override fun showDiagnosisView() {
    this.diabetesManagementEnabled = true
  }

  override fun hideDiagnosisView() {
    this.diabetesManagementEnabled = false
  }

  override fun showCurrentSmokerQuestion() {
    showSmokerQuestion = true
  }

  override fun hideCurrentSmokerQuestion() {
    showSmokerQuestion = false
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
