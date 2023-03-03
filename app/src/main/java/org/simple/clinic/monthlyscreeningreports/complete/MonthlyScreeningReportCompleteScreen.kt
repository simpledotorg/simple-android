package org.simple.clinic.monthlyscreeningreports.complete

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
import org.simple.clinic.databinding.ScreenMonthlyScreeningReportCompleteBinding
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.SubmittedDate
import org.simple.clinic.di.injector
import org.simple.clinic.monthlyscreeningreports.list.MonthlyScreeningReportListScreen
import org.simple.clinic.monthlyscreeningreports.util.formatScreeningMonthStringToLocalDate
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.UiEvent
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class MonthlyScreeningReportCompleteScreen : BaseScreen<
    MonthlyScreeningReportCompleteScreen.Key,
    ScreenMonthlyScreeningReportCompleteBinding,
    MonthlyScreeningReportCompleteModel,
    MonthlyScreeningReportCompleteEvent,
    MonthlyScreeningReportCompleteEffect,
    MonthlyScreeningReportCompleteViewEffect>(),
    MonthlyScreeningReportCompleteUi,
    HandlesBack {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  @DateFormatter(SubmittedDate)
  lateinit var submittedDateFormatter: DateTimeFormatter

  @Inject
  lateinit var effectHandlerFactory: MonthlyScreeningReportCompleteEffectHandler.Factory

  private val monthSubmittedTextView
    get() = binding.monthSubmittedTextView

  private val doneButton
    get() = binding.doneButton

  private val hardwareBackClicks = PublishSubject.create<Unit>()

  override fun defaultModel() = MonthlyScreeningReportCompleteModel.default()

  override fun createUpdate() = MonthlyScreeningReportCompleteUpdate()

  override fun createInit() = MonthlyScreeningReportCompleteInit(screenKey.id)

  override fun createEffectHandler(viewEffectsConsumer: Consumer<MonthlyScreeningReportCompleteViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun viewEffectHandler() = MonthlyScreeningReportCompleteViewEffectHandler(this)

  override fun events(): Observable<MonthlyScreeningReportCompleteEvent> {
    return Observable
        .mergeArray(
            doneClicks(),
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun uiRenderer() = MonthlyScreeningReportCompleteUiRenderer(this)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenMonthlyScreeningReportCompleteBinding.inflate(layoutInflater, container, false)

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
      override val analyticsName: String = "Monthly Screening Reports Complete Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = MonthlyScreeningReportCompleteScreen()
  }

  interface Injector {
    fun inject(target: MonthlyScreeningReportCompleteScreen)
  }

  override fun showMonthCompletedView(response: QuestionnaireResponse) {
    monthSubmittedTextView.text =
        context?.resources?.getString(
            R.string.monthly_screening_reports_submitted_with_date,
            submittedDateFormatter.format(formatScreeningMonthStringToLocalDate(response.content))
        )
  }

  override fun goToMonthlyReportListScreen() {
    router.popUntil(MonthlyScreeningReportListScreen.Key())
  }

  override fun onBackPressed(): Boolean {
    hardwareBackClicks.onNext(Unit)
    return true
  }
}
