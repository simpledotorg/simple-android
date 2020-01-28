package org.simple.clinic.summary.bloodpressures.newbpsummary.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.patientsummary_newbpsummary_content.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.bp.history.BloodPressureHistoryScreenKey
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.summary.SUMMARY_REQCODE_BP_ENTRY
import org.simple.clinic.summary.bloodpressures.newbpsummary.AddNewBloodPressureClicked
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewConfig
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewEffect
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewEffectHandler
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewEvent
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewInit
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewModel
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewUi
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewUiActions
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewUiRenderer
import org.simple.clinic.summary.bloodpressures.newbpsummary.NewBloodPressureSummaryViewUpdate
import org.simple.clinic.summary.bloodpressures.newbpsummary.SeeAllClicked
import org.simple.clinic.util.unsafeLazy
import java.util.UUID
import javax.inject.Inject

class NewBloodPressureSummaryView(
    context: Context,
    attrs: AttributeSet
) : CardView(context, attrs), NewBloodPressureSummaryViewUi, NewBloodPressureSummaryViewUiActions {

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var bloodPressureSummaryConfig: NewBloodPressureSummaryViewConfig

  @Inject
  lateinit var effectHandlerFactory: NewBloodPressureSummaryViewEffectHandler.Factory

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var crashReporter: CrashReporter

  private val viewEvents = PublishSubject.create<NewBloodPressureSummaryViewEvent>()

  private val events: Observable<NewBloodPressureSummaryViewEvent> by unsafeLazy {
    Observable
        .merge(
            addNewBpClicked(),
            seeAllClicked(),
            viewEvents
        )
        .compose(ReportAnalyticsEvents())
        .cast<NewBloodPressureSummaryViewEvent>()
  }

  private val uiRenderer: NewBloodPressureSummaryViewUiRenderer by unsafeLazy {
    NewBloodPressureSummaryViewUiRenderer(this, bloodPressureSummaryConfig)
  }

  private val delegate: MobiusDelegate<NewBloodPressureSummaryViewModel, NewBloodPressureSummaryViewEvent, NewBloodPressureSummaryViewEffect> by unsafeLazy {
    val screenKey = screenRouter.key<PatientSummaryScreenKey>(this)
    MobiusDelegate(
        events = events,
        defaultModel = NewBloodPressureSummaryViewModel.create(screenKey.patientUuid),
        init = NewBloodPressureSummaryViewInit(bloodPressureSummaryConfig),
        update = NewBloodPressureSummaryViewUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render,
        crashReporter = crashReporter
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<NewBloodPressureSummaryViewInjector>().inject(this)

    delegate.prepare()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun showNoBloodPressuresView() {
  }

  override fun showBloodPressures(bloodPressures: List<BloodPressureMeasurement>) {
  }

  override fun showSeeAllButton() {
  }

  override fun hideSeeAllButton() {
  }

  override fun openBloodPressureEntrySheet(patientUuid: UUID) {
    val intent = BloodPressureEntrySheet.intentForNewBp(context, patientUuid)
    activity.startActivityForResult(intent, SUMMARY_REQCODE_BP_ENTRY)
  }

  override fun openBloodPressureUpdateSheet(bpUuid: UUID) {
    val intent = BloodPressureEntrySheet.intentForUpdateBp(context, bpUuid)
    activity.startActivity(intent)
  }

  override fun showBloodPressureHistoryScreen(patientUuid: UUID) {
    screenRouter.push(BloodPressureHistoryScreenKey(patientUuid))
  }

  private fun addNewBpClicked(): Observable<NewBloodPressureSummaryViewEvent> {
    return addNewBP.clicks().map { AddNewBloodPressureClicked }
  }

  private fun seeAllClicked(): Observable<NewBloodPressureSummaryViewEvent> {
    return seeAll.clicks().map { SeeAllClicked }
  }
}
