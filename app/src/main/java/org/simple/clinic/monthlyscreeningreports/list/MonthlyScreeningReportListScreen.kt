package org.simple.clinic.monthlyscreeningreports.list

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
import org.simple.clinic.databinding.MonthlyScreeningReportItemViewBinding
import org.simple.clinic.databinding.ScreenMonthlyScreeningReportListBinding
import org.simple.clinic.di.injector
import org.simple.clinic.monthlyscreeningreports.form.QuestionnaireEntryScreen
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class MonthlyScreeningReportListScreen : BaseScreen<
    MonthlyScreeningReportListScreen.Key,
    ScreenMonthlyScreeningReportListBinding,
    MonthlyScreeningReportListModel,
    MonthlyScreeningReportListEvent,
    MonthlyScreeningReportListEffect,
    MonthlyScreeningReportListViewEffect>(), MonthlyScreeningReportListUi {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var effectHandlerFactory: MonthlyScreeningReportListEffectHandler.Factory

  private val backButton
    get() = binding.backButton

  private val facilityTextView
    get() = binding.facilityTextView

  private val monthlyReportRecyclerView
    get() = binding.monthlyReportRecyclerView

  private val monthlyReportItemAdapter = ItemAdapter(
      diffCallback = MonthlyScreeningReportItem.DiffCallback(),
      bindings = mapOf(
          R.layout.monthly_screening_report_item_view to { layoutInflater, parent ->
            MonthlyScreeningReportItemViewBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  override fun defaultModel() = MonthlyScreeningReportListModel.default()

  override fun createInit() = MonthlyScreeningReportListInit()

  override fun createUpdate() = MonthlyScreeningReportListUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<MonthlyScreeningReportListViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun viewEffectHandler() = MonthlyScreeningReportListViewEffectHandler(this)

  override fun events(): Observable<MonthlyScreeningReportListEvent> {
    return Observable
        .mergeArray(
            backClicks(),
            reportSelection()
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun uiRenderer() = MonthlyScreeningReportListUiRenderer(this)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenMonthlyScreeningReportListBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    monthlyReportRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = monthlyReportItemAdapter
    }
  }

  private fun backClicks(): Observable<UiEvent> {
    return backButton.clicks()
        .map {
          BackButtonClicked
        }
  }

  private fun reportSelection(): Observable<MonthlyScreeningReportListEvent> {
    return monthlyReportItemAdapter
        .itemEvents
        .ofType<MonthlyScreeningReportItem.Event.ListItemClicked>()
        .map { it.id }
        .map(::MonthlyScreeningReportItemClicked)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Monthly Screening Reports List Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = MonthlyScreeningReportListScreen()
  }

  override fun setFacility(facilityName: String) {
    facilityTextView.text = facilityName
  }

  override fun displayMonthlyReportList(responseList: List<QuestionnaireResponse>) {
    val reportList = MonthlyScreeningReportItem.from(responseList)
    monthlyReportItemAdapter.submitList(reportList)
  }

  override fun showProgress() {
  }

  override fun hideProgress() {
  }

  override fun openMonthlyScreeningForm(uuid: UUID) {
    router.push(
        QuestionnaireEntryScreen.Key(
            id = uuid,
            questionnaireType = MonthlyScreeningReports
        )
    )
  }

  override fun goBack() {
    router.pop()
  }

  interface Injector {
    fun inject(target: MonthlyScreeningReportListScreen)
  }
}
