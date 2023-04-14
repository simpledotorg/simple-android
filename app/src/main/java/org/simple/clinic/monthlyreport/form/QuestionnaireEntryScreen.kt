package org.simple.clinic.monthlyreport.form

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenQuestionnaireEntryFormBinding
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.MonthAndYear
import org.simple.clinic.di.DateFormatter.Type.FormSubmissionDateTime
import org.simple.clinic.di.injector
import org.simple.clinic.monthlyreport.complete.MonthlyReportCompleteScreen
import org.simple.clinic.monthlyreport.form.compose.QuestionnaireFormContainer
import org.simple.clinic.monthlyreport.util.getMonthlyReportFormattedMonthString
import org.simple.clinic.monthlyreport.util.getMonthlyReportSubmitStatus
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaire.component.BaseComponentData
import org.simple.clinic.questionnaire.component.ViewGroupComponentData
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.questionnaire.MonthlyDrugReports
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.MonthlySuppliesReports
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateTimeAtZone
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.visibleOrGone
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class QuestionnaireEntryScreen : BaseScreen<
    QuestionnaireEntryScreen.Key,
    ScreenQuestionnaireEntryFormBinding,
    QuestionnaireEntryModel,
    QuestionnaireEntryEvent,
    QuestionnaireEntryEffect,
    QuestionnaireEntryViewEffect>(),
    QuestionnaireEntryUi,
    HandlesBack {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var userClock: UserClock

  @Inject
  @DateFormatter(MonthAndYear)
  lateinit var monthAndYearDateFormatter: DateTimeFormatter

  @Inject
  @DateFormatter(FormSubmissionDateTime)
  lateinit var formSubmissionDateTimeFormatter: DateTimeFormatter

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var effectHandlerFactory: QuestionnaireEntryEffectHandler.Factory

  var content = mutableMapOf<String, Any?>()

  private val backButton
    get() = binding.backButton

  private val monthTextView
    get() = binding.monthTextView

  private val facilityTextView
    get() = binding.facilityTextView

  private val submittedDateAndTimeContainer
    get() = binding.submittedDateAndTimeContainer

  private val submittedDateAndTimeTextView
    get() = binding.submittedDateAndTimeTextView

  private val submitButton
    get() = binding.submitButton

  private val hotEvents = PublishSubject.create<QuestionnaireEntryEvent>()
  private val hardwareBackClicks = PublishSubject.create<Unit>()

  override fun defaultModel() = QuestionnaireEntryModel.from(questionnaireResponse = screenKey.questionnaireResponse)

  override fun createInit() = QuestionnaireEntryInit(screenKey.questionnaireType)

  override fun createUpdate() = QuestionnaireEntryUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<QuestionnaireEntryViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun viewEffectHandler() = QuestionnaireEntryViewEffectHandler(this)

  override fun events(): Observable<QuestionnaireEntryEvent> {
    return Observable
        .mergeArray(
            backClicks(),
            submitClicks(),
            hotEvents
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun uiRenderer() = QuestionnaireEntryUiRenderer(this)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenQuestionnaireEntryFormBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setToolbarMonth(screenKey.questionnaireResponse)
    setSubmittedView(screenKey.questionnaireResponse)
  }

  @Parcelize
  data class Key(
      val questionnaireType: QuestionnaireType,
      val questionnaireResponse: QuestionnaireResponse,
      override val analyticsName: String = "$questionnaireType questionnaire entry form"
  ) : ScreenKey() {

    override fun instantiateFragment() = QuestionnaireEntryScreen()
  }

  override fun setFacility(facilityName: String) {
    facilityTextView.text = facilityName
  }

  private fun setToolbarMonth(response: QuestionnaireResponse) {
    val stringResource = when (screenKey.questionnaireType) {
      is MonthlyScreeningReports -> R.string.monthly_screening_reports_screening_report
      is MonthlySuppliesReports -> R.string.monthly_supplies_report_supplies_report
      is MonthlyDrugReports -> R.string.monthly_drug_stock_reports_drug_stock_reports
      else -> R.string.monthly_screening_reports_screening_report
    }
    monthTextView.text = context?.resources?.getString(
        stringResource,
        getMonthlyReportFormattedMonthString(response.content, monthAndYearDateFormatter)
    )
  }

  private fun setSubmittedView(response: QuestionnaireResponse) {
    val isSubmitted = getMonthlyReportSubmitStatus(response.content)
    submittedDateAndTimeContainer.visibleOrGone(isSubmitted)

    if (isSubmitted) {
      val updatedAt = response.timestamps.updatedAt
      submittedDateAndTimeTextView.text = context?.resources?.getString(
          R.string.reports_submitted_with_date_and_time,
          formSubmissionDateTimeFormatter.format(updatedAt.toLocalDateTimeAtZone(userClock.zone)))
    }
  }

  override fun displayQuestionnaireFormLayout(layout: BaseComponentData) {
    content = screenKey.questionnaireResponse.content.toMutableMap()

    if (layout is ViewGroupComponentData) {
      binding.composeView.apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
          QuestionnaireFormContainer(
              viewGroupComponentData = layout,
              content = content
          )
        }
      }
    }
  }

  private fun backClicks(): Observable<UiEvent> {
    return backButton.clicks()
        .mergeWith(hardwareBackClicks)
        .map {
          QuestionnaireEntryBackClicked
        }
  }

  private fun submitClicks(): Observable<UiEvent> {
    return submitButton.clicks()
        .map { SubmitButtonClicked(content) }
  }

  override fun goBack() {
    binding.root.hideKeyboard()
    router.pop()
  }

  override fun showUnsavedChangesWarningDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.reports_unsaved_changes_title)
        .setMessage(R.string.reports_unsaved_changes_text)
        .setPositiveButton(R.string.reports_stay_title, null)
        .setNegativeButton(R.string.reports_leave_page_title) { _, _ ->
          hotEvents.onNext(UnsavedChangesWarningLeavePageClicked)
        }
        .show()
  }

  override fun goToMonthlyReportsCompleteScreen() {
    router.push(MonthlyReportCompleteScreen.Key(screenKey.questionnaireResponse.uuid, screenKey.questionnaireType))
  }

  interface Injector {
    fun inject(target: QuestionnaireEntryScreen)
  }

  override fun onBackPressed(): Boolean {
    hardwareBackClicks.onNext(Unit)
    return true
  }
}
