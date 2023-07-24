package org.simple.clinic.monthlyreports.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.MonthlyReportListItemViewBinding
import org.simple.clinic.databinding.ScreenMonthlyReportsBinding
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.MonthAndYear
import org.simple.clinic.di.injector
import org.simple.clinic.monthlyreports.form.QuestionnaireEntryScreen
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.questionnaire.DrugStockReports
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.MonthlySuppliesReports
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MonthlyReportsScreen : BaseScreen<
    MonthlyReportsScreen.Key,
    ScreenMonthlyReportsBinding,
    MonthlyReportsModel,
    MonthlyReportsEvent,
    MonthlyReportsEffect,
    MonthlyReportsViewEffect>(), MonthlyReportsUi {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  @DateFormatter(MonthAndYear)
  lateinit var monthAndYearDateFormatter: DateTimeFormatter

  @Inject
  lateinit var effectHandlerFactory: MonthlyReportsEffectHandler.Factory

  private val backButton
    get() = binding.backButton

  private val toolbarTitleTextView
    get() = binding.toolbarTitleTextView

  private val facilityTextView
    get() = binding.facilityTextView

  private val monthlyReportsRecyclerView
    get() = binding.monthlyReportsRecyclerView

  private val monthlyReportItemAdapter = ItemAdapter(
      diffCallback = MonthlyReportItem.DiffCallback(),
      bindings = mapOf(
          R.layout.monthly_report_list_item_view to { layoutInflater, parent ->
            MonthlyReportListItemViewBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  override fun defaultModel() = MonthlyReportsModel.default()

  override fun createInit() = MonthlyReportsInit(screenKey.questionnaireType)

  override fun createUpdate() = MonthlyReportsUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<MonthlyReportsViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun viewEffectHandler() = MonthlyReportsViewEffectHandler(this)

  override fun events(): Observable<MonthlyReportsEvent> {
    return Observable
        .mergeArray(
            backClicks(),
            reportSelection()
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun uiRenderer() = MonthlyReportsUiRenderer(this)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenMonthlyReportsBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    monthlyReportsRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = monthlyReportItemAdapter
    }
    setToolbarTitle(screenKey.questionnaireType)
  }

  private fun setToolbarTitle(questionnaireType: QuestionnaireType) {
    toolbarTitleTextView.text = when (questionnaireType) {
      is MonthlyScreeningReports -> requireContext().resources.getString(
          R.string.monthly_screening_reports,
      )

      is MonthlySuppliesReports -> requireContext().resources.getString(
          R.string.monthly_supplies_reports,
      )

      is DrugStockReports -> requireContext().resources.getString(
          R.string.monthly_drug_stock_reports,
      )

      else -> ""
    }
  }

  private fun backClicks(): Observable<UiEvent> {
    return backButton.clicks()
        .map {
          BackButtonClicked
        }
  }

  private fun reportSelection(): Observable<MonthlyReportsEvent> {
    return monthlyReportItemAdapter
        .itemEvents
        .ofType<MonthlyReportItem.Event.ListItemClicked>()
        .map { it.questionnaireResponse }
        .map {
          MonthlyReportItemClicked(screenKey.questionnaireType, it)
        }
  }

  @Parcelize
  data class Key(
      val questionnaireType: QuestionnaireType,
      override val analyticsName: String = "$questionnaireType List Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = MonthlyReportsScreen()
  }

  override fun setFacility(facilityName: String) {
    facilityTextView.text = facilityName
  }

  override fun displayMonthlyReports(monthlyReports: List<QuestionnaireResponse>) {
    val reportList = MonthlyReportItem.from(monthlyReports, monthAndYearDateFormatter)
    monthlyReportItemAdapter.submitList(reportList)
  }

  override fun openMonthlyReportForm(questionnaireType: QuestionnaireType, questionnaireResponse: QuestionnaireResponse) {
    router.push(
        QuestionnaireEntryScreen.Key(
            questionnaireType = questionnaireType,
            questionnaireResponse = questionnaireResponse
        )
    )
  }

  override fun goBack() {
    router.pop()
  }

  override fun onDestroyView() {
    monthlyReportsRecyclerView.adapter = null
    super.onDestroyView()
  }

  interface Injector {
    fun inject(target: MonthlyReportsScreen)
  }
}
