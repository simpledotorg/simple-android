package org.simple.clinic.home.patients.links

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.databinding.PatientsTabLinkViewBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.monthlyreport.list.MonthlyReportListScreen
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.patient.download.formatdialog.SelectLineListFormatDialog
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.MonthlySuppliesReports
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class PatientsTabLinkView(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs), PatientsTabLinkUi, PatientsTabLinkUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: PatientsTabLinkEffectHandler.Factory

  @Inject
  lateinit var uiRendererFactory: PatientsTabLinkUiRenderer.Factory

  @Inject
  lateinit var country: Country

  @Inject
  lateinit var features: Features

  private val events: Observable<PatientsTabLinkEvent> by unsafeLazy {
    Observable
        .merge(
            monthlyScreeningReportContainerClick(),
            monthlySuppliesReportsContainerClick(),
            downloadPatientLineListContainerClick()
        )
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  private val delegate by unsafeLazy {
    val uiRenderer = uiRendererFactory.create(
        this,
        isPatientLineListEnabled = features.isEnabled(Feature.PatientLineListDownload)
            && country.isoCountryCode == Country.INDIA
    )

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = PatientsTabLinkModel.default(),
        update = PatientsTabLinkUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = PatientsTabLinkInit(
            isMonthlyScreeningReportsEnabled = features.isEnabled(Feature.MonthlyScreeningReportsEnabled)
        ),
        modelUpdateListener = uiRenderer::render
    )
  }

  private val binding = PatientsTabLinkViewBinding.inflate(LayoutInflater.from(context),
      this)

  private val patientTabLinksContainer
    get() = binding.patientTabLinksContainer

  private val monthlyScreeningReportContainer
    get() = binding.monthlyScreeningReportContainer

  private val monthlySuppliesReportsContainer
    get() = binding.monthlySuppliesReportsContainer

  private val downloadPatientLineListContainer
    get() = binding.downloadPatientLineListContainer

  private fun monthlyScreeningReportContainerClick(): Observable<UiEvent> {
    return monthlyScreeningReportContainer.clicks()
        .map {
          MonthlyScreeningReportsClicked
        }
  }

  private fun monthlySuppliesReportsContainerClick(): Observable<UiEvent> {
    return monthlySuppliesReportsContainer.clicks()
        .map {
          MonthlySuppliesReportsClicked
        }
  }

  private fun downloadPatientLineListContainerClick(): Observable<UiEvent> {
    return downloadPatientLineListContainer.clicks()
        .map {
          DownloadPatientLineListClicked()
        }
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)
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

  interface Injector {
    fun inject(target: PatientsTabLinkView)
  }

  override fun showOrHideLinkView(isVisible: Boolean) {
    patientTabLinksContainer.visibleOrGone(isVisible)
  }

  override fun showOrHideMonthlyScreeningReportsView(isVisible: Boolean) {
    monthlyScreeningReportContainer.visibleOrGone(isVisible)
  }

  override fun showOrHidePatientLineListDownload(isVisible: Boolean) {
    downloadPatientLineListContainer.visibleOrGone(isVisible)
  }

  override fun openMonthlyScreeningReports() {
    router.push(MonthlyReportListScreen.Key(MonthlyScreeningReports))
  }

  override fun openMonthlySuppliesReports() {
    router.push(MonthlyReportListScreen.Key(MonthlySuppliesReports))
  }

  override fun openPatientLineListDownloadDialog() {
    router.push(SelectLineListFormatDialog.Key())
  }
}
