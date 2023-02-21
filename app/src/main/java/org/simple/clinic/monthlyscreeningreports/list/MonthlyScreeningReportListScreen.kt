package org.simple.clinic.monthlyscreeningreports.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenMonthlyReportListBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class MonthlyScreeningReportListScreen : BaseScreen<
    MonthlyScreeningReportListScreen.Key,
    ScreenMonthlyReportListBinding,
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
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun uiRenderer() = MonthlyScreeningReportListUiRenderer(this)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenMonthlyReportListBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  private fun backClicks(): Observable<UiEvent> {
    return backButton.clicks()
        .map {
          BackButtonClicked
        }
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
    //todo
  }

  override fun showProgress() {
  }

  override fun hideProgress() {
  }

  override fun goBack() {
    router.pop()
  }

  interface Injector {
    fun inject(target: MonthlyScreeningReportListScreen)
  }
}
