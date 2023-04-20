package org.simple.clinic.monthlyreports.complete

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenMonthlyReportCompleteBinding
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.FullMonthAndYear
import org.simple.clinic.di.injector
import org.simple.clinic.monthlyreports.list.MonthlyReportsScreen
import org.simple.clinic.monthlyreports.util.parseMonthlyReportMonthStringToLocalDate
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.UiEvent
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class MonthlyReportCompleteScreen : BaseScreen<
    MonthlyReportCompleteScreen.Key,
    ScreenMonthlyReportCompleteBinding,
    MonthlyReportCompleteModel,
    MonthlyReportCompleteEvent,
    MonthlyReportCompleteEffect,
    MonthlyReportCompleteViewEffect>(),
    MonthlyReportCompleteUi,
    HandlesBack {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  @DateFormatter(FullMonthAndYear)
  lateinit var fullMonthAndYearFormatter: DateTimeFormatter

  @Inject
  lateinit var effectHandlerFactory: MonthlyReportCompleteEffectHandler.Factory

  private val monthSubmittedTextView
    get() = binding.monthSubmittedTextView

  private val doneButton
    get() = binding.doneButton

  private val hardwareBackClicks = PublishSubject.create<Unit>()

  override fun defaultModel() = MonthlyReportCompleteModel.default()

  override fun createUpdate() = MonthlyReportCompleteUpdate()

  override fun createInit() = MonthlyReportCompleteInit(screenKey.id)

  override fun createEffectHandler(viewEffectsConsumer: Consumer<MonthlyReportCompleteViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun viewEffectHandler() = MonthlyReportCompleteViewEffectHandler(this)

  override fun events(): Observable<MonthlyReportCompleteEvent> {
    return Observable
        .mergeArray(
            doneClicks(),
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun uiRenderer() = MonthlyReportCompleteUiRenderer(this)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenMonthlyReportCompleteBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  private fun doneClicks(): Observable<UiEvent> {
    return doneButton.clicks()
        .mergeWith(hardwareBackClicks)
        .map {
          DoneButtonClicked
        }
  }

  @Parcelize
  data class Key(
      val id: UUID,
      val questionnaireType: QuestionnaireType,
      override val analyticsName: String = "$questionnaireType Complete Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = MonthlyReportCompleteScreen()
  }

  interface Injector {
    fun inject(target: MonthlyReportCompleteScreen)
  }

  override fun showFormSubmissionMonthAndYearTextView(response: QuestionnaireResponse) {
    monthSubmittedTextView.text =
        context?.resources?.getString(
            R.string.reports_submitted_with_date,
            fullMonthAndYearFormatter.format(parseMonthlyReportMonthStringToLocalDate(response.content))
        )
  }

  override fun goToMonthlyReportListScreen() {
    router.popUntil(MonthlyReportsScreen.Key(screenKey.questionnaireType))
  }

  override fun onBackPressed(): Boolean {
    hardwareBackClicks.onNext(Unit)
    return true
  }
}
