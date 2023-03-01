package org.simple.clinic.monthlyscreeningreports.form

import android.content.Context
import android.view.LayoutInflater
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
import org.simple.clinic.di.DateFormatter.Type.SubmittedDateTime
import org.simple.clinic.di.injector
import org.simple.clinic.monthlyscreeningreports.complete.MonthlyScreeningReportCompleteScreen
import org.simple.clinic.monthlyscreeningreports.form.compose.QuestionnaireFormContainer
import org.simple.clinic.monthlyscreeningreports.util.getScreeningMonth
import org.simple.clinic.monthlyscreeningreports.util.getScreeningSubmitStatus
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaire.component.BaseComponentData
import org.simple.clinic.questionnaire.component.ViewGroupComponentData
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toLocalDateTimeAtZone
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import java.time.format.DateTimeFormatter
import java.util.UUID
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
  @DateFormatter(SubmittedDateTime)
  lateinit var submittedDateTimeFormatter: DateTimeFormatter

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var effectHandlerFactory: QuestionnaireEntryEffectHandler.Factory

  var questionnaireResponse: QuestionnaireResponse? = null

  var content = mutableMapOf<String, Any>()

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

  //  private val questionnaireFormController: QuestionnaireEntryFormController by lazy {
  //    QuestionnaireEntryFormController {
  //      questionnaireResponse?.content = questionnaireResponse?.content?.plus(it)!!
  //      val viewGroup = preFillLayout(questionnaireLayout!!, questionnaireResponse!!)
  //      if (viewGroup != null) {
  //        questionnaireFormController.setData(viewGroup.children)
  //      }
  //    }
  //  }

  override fun defaultModel() = QuestionnaireEntryModel.default()

  override fun createInit() = QuestionnaireEntryInit(screenKey.questionnaireType, screenKey.id)

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

  @Parcelize
  data class Key(
      val id: UUID,
      val questionnaireType: QuestionnaireType,
      override val analyticsName: String = "$questionnaireType questionnaire entry form"
  ) : ScreenKey() {

    override fun instantiateFragment() = QuestionnaireEntryScreen()
  }

  override fun setFacility(facilityName: String) {
    facilityTextView.text = facilityName
  }

  override fun displayQuestionnaireFormLayout(layout: BaseComponentData, response: QuestionnaireResponse) {
    setMonthTextView(response)
    setSubmittedDateAndTimeView(response)

    questionnaireResponse = response
    content = requireNotNull(questionnaireResponse).content.toMutableMap()
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

  private fun setMonthTextView(response: QuestionnaireResponse) {
    monthTextView.text = context?.resources?.getString(
        R.string.monthly_screening_reports_screening_report,
        getScreeningMonth(response.content, monthAndYearDateFormatter)
    )
  }

  private fun setSubmittedDateAndTimeView(response: QuestionnaireResponse) {
    val isSubmitted = getScreeningSubmitStatus(response.content)
    submittedDateAndTimeContainer.visibleOrGone(isSubmitted)

    if (isSubmitted) {
      val updatedAt = response.timestamps.updatedAt
      submittedDateAndTimeTextView.text = context?.resources?.getString(
          R.string.monthly_screening_reports_submitted_with_date_and_time,
          submittedDateTimeFormatter.format(updatedAt.toLocalDateTimeAtZone(userClock.zone)))
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
        .map {
          content["submitted"] = true
          //          questionnaireResponse?.content?.put("submitted", true)
          SubmitButtonClicked(requireNotNull(questionnaireResponse))
        }
  }

  override fun showProgress() {
  }

  override fun hideProgress() {
  }

  override fun goBack() {
    router.pop()
  }

  override fun showUnsavedChangesWarningDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.monthly_screening_reports_unsaved_changes_title)
        .setMessage(R.string.monthly_screening_reports_unsaved_changes_text)
        .setPositiveButton(R.string.monthly_screening_reports_stay_title, null)
        .setNegativeButton(R.string.monthly_screening_reports_leave_page_title) { _, _ ->
          hotEvents.onNext(UnsavedChangesWarningLeavePageClicked)
        }
        .show()
  }

  override fun goToMonthlyReportsCompleteScreen() {
    router.push(MonthlyScreeningReportCompleteScreen.Key("October 2022"))
  }

  interface Injector {
    fun inject(target: QuestionnaireEntryScreen)
  }

  override fun onBackPressed(): Boolean {
    hardwareBackClicks.onNext(Unit)
    return true
  }
}
