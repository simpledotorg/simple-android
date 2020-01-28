package org.simple.clinic.summary.bloodpressures.newbpsummary.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.patientsummary_newbpsummary_content.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.bp.history.BloodPressureHistoryScreenKey
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.summary.SUMMARY_REQCODE_BP_ENTRY
import org.simple.clinic.summary.bloodpressures.newbpsummary.BloodPressureClicked
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
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.util.unsafeLazy
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class NewBloodPressureSummaryView(
    context: Context,
    attrs: AttributeSet
) : CardView(context, attrs), NewBloodPressureSummaryViewUi, NewBloodPressureSummaryViewUiActions {

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var bloodPressureSummaryConfig: NewBloodPressureSummaryViewConfig

  @Inject
  lateinit var patientSummaryConfig: PatientSummaryConfig

  @Inject
  lateinit var effectHandlerFactory: NewBloodPressureSummaryViewEffectHandler.Factory

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var userClock: UserClock

  @field:[Inject Named("date_for_bp_history")]
  lateinit var dateFormatter: DateTimeFormatter

  @field:[Inject Named("time_for_bp_history")]
  lateinit var timeFormatter: DateTimeFormatter

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

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_newbpsummary_content, this, true)
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
    newBPItemContainer.visibility = View.GONE
    placeHolderMessageTextView.visibility = View.VISIBLE
  }

  override fun showBloodPressures(bloodPressures: List<BloodPressureMeasurement>) {
    newBPItemContainer.visibility = View.VISIBLE
    placeHolderMessageTextView.visibility = View.GONE

    render(
        measurements = bloodPressures,
        canEditFor = patientSummaryConfig.bpEditableDuration,
        utcClock = utcClock,
        userClock = userClock,
        dateFormatter = dateFormatter,
        timeFormatter = timeFormatter
    )
  }

  override fun showSeeAllButton() {
    seeAll.visibility = View.VISIBLE
  }

  override fun hideSeeAllButton() {
    seeAll.visibility = View.GONE
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

  private fun render(
      measurements: List<BloodPressureMeasurement>,
      canEditFor: Duration,
      utcClock: UtcClock,
      userClock: UserClock,
      dateFormatter: DateTimeFormatter,
      timeFormatter: DateTimeFormatter
  ) {
    val listItemViews = generateBpViews(
        measurements = measurements,
        canEditFor = canEditFor,
        utcClock = utcClock,
        userClock = userClock,
        dateFormatter = dateFormatter,
        timeFormatter = timeFormatter
    )
    newBPItemContainer.removeAllViews()
    listItemViews.forEach(newBPItemContainer::addView)
  }

  private fun generateBpViews(
      measurements: List<BloodPressureMeasurement>,
      canEditFor: Duration,
      utcClock: UtcClock,
      userClock: UserClock,
      dateFormatter: DateTimeFormatter,
      timeFormatter: DateTimeFormatter
  ): List<View> {
    val measurementByDate = measurements.groupBy { it.recordedAt.toLocalDateAtZone(userClock.zone) }

    return measurementByDate.mapValues { (_, measurementsList) ->
      val hasMultipleMeasurementsInSameDate = measurementsList.size > 1
      measurementsList.map { measurement ->
        val isBpEditable = isBpEditable(measurement, canEditFor, utcClock)
        val recordedAt = measurement.recordedAt.toLocalDateAtZone(userClock.zone)
        val bpTime = if (hasMultipleMeasurementsInSameDate) {
          timeFormatter.format(measurement.recordedAt.atZone(userClock.zone))
        } else {
          null
        }

        val bloodPressureItemView = LayoutInflater.from(context).inflate(R.layout.list_patientsummary_newbp_measurement, this, false) as NewBloodPressureItemView
        bloodPressureItemView.render(
            measurement = measurement,
            isBpEditable = isBpEditable,
            bpDate = dateFormatter.format(recordedAt),
            bpTime = bpTime,
            editMeasurementClicked = { clickedMeasurement -> viewEvents.onNext(BloodPressureClicked(clickedMeasurement)) }
        )
        bloodPressureItemView
      }
    }.values.flatten()
  }

  private fun isBpEditable(
      bloodPressureMeasurement: BloodPressureMeasurement,
      bpEditableFor: Duration,
      utcClock: UtcClock
  ): Boolean {
    val now = Instant.now(utcClock)
    val createdAt = bloodPressureMeasurement.createdAt

    val durationSinceBpCreated = Duration.between(createdAt, now)

    return durationSinceBpCreated <= bpEditableFor
  }
}
