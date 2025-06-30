package org.simple.clinic.summary.bloodpressures.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.card.MaterialCardView
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.bp.history.BloodPressureHistoryScreen
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation.ContinueToActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.ActivityResult
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.summary.PatientSummaryChildView
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.summary.PatientSummaryModelUpdateCallback
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.summary.SUMMARY_REQCODE_BP_ENTRY
import org.simple.clinic.summary.bloodpressures.AddNewBloodPressureClicked
import org.simple.clinic.summary.bloodpressures.BloodPressureClicked
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewConfig
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewEffect
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewEffectHandler
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewEvent
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewInit
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewModel
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewUi
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewUiActions
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewUiRenderer
import org.simple.clinic.summary.bloodpressures.BloodPressureSummaryViewUpdate
import org.simple.clinic.summary.bloodpressures.SeeAllClicked
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

private typealias BpRecorded = () -> Unit

class BloodPressureSummaryView(
    context: Context,
    attrs: AttributeSet
) : MaterialCardView(context, attrs), BloodPressureSummaryViewUi, BloodPressureSummaryViewUiActions, PatientSummaryChildView {

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var bloodPressureSummaryConfig: BloodPressureSummaryViewConfig

  @Inject
  lateinit var patientSummaryConfig: PatientSummaryConfig

  @Inject
  lateinit var effectHandlerFactory: BloodPressureSummaryViewEffectHandler.Factory

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var userClock: UserClock

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  @Named("time_for_measurement_history")
  lateinit var timeFormatter: DateTimeFormatter

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private val viewEvents = PublishSubject.create<BloodPressureSummaryViewEvent>()

  private var summaryItems by mutableStateOf(emptyList<BloodPressureSummaryItem>())
  private var canShowSeeAllButton by mutableStateOf(false)

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  private val events: Observable<BloodPressureSummaryViewEvent> by unsafeLazy {
    viewEvents
        .compose(ReportAnalyticsEvents())
        .cast<BloodPressureSummaryViewEvent>()
  }

  private val uiRenderer: BloodPressureSummaryViewUiRenderer by unsafeLazy {
    BloodPressureSummaryViewUiRenderer(this, bloodPressureSummaryConfig)
  }

  private val delegate: MobiusDelegate<BloodPressureSummaryViewModel, BloodPressureSummaryViewEvent, BloodPressureSummaryViewEffect> by unsafeLazy {
    val screenKey = screenKeyProvider.keyFor<PatientSummaryScreenKey>(this)

    MobiusDelegate.forView(
        events = events,
        defaultModel = BloodPressureSummaryViewModel.create(screenKey.patientUuid),
        init = BloodPressureSummaryViewInit(bloodPressureSummaryConfig),
        update = BloodPressureSummaryViewUpdate(bloodPressureSummaryConfig),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = { model ->
          modelUpdateCallback?.invoke(model)
          uiRenderer.render(model)
        })
  }

  var bpRecorded: BpRecorded? = null

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<BloodPressureSummaryViewInjector>().inject(this)

    val screenDestroys: Observable<ScreenDestroyed> = detaches().map { ScreenDestroyed }

    setupBpRecordedEvents(screenDestroys)

    addView(ComposeView(context).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

      setContent {
        SimpleTheme {
          BloodPressureSummary(
              summaryItems = summaryItems,
              canShowSeeAllButton = canShowSeeAllButton,
              onSeeAllClick = {
                viewEvents.onNext(SeeAllClicked)
              },
              onAddBPClick = {
                viewEvents.onNext(AddNewBloodPressureClicked)
              },
              onEditBPClick = { id ->
                viewEvents.onNext(BloodPressureClicked(id))
              }
          )
        }
      }
    })
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

  override fun showBloodPressures(bloodPressures: List<BloodPressureMeasurement>) {
    val bpSummaryItems = generateBPSummaryItems(
        measurements = bloodPressures,
        canEditFor = patientSummaryConfig.bpEditableDuration,
        utcClock = utcClock,
        userClock = userClock,
        dateFormatter = dateFormatter,
        timeFormatter = timeFormatter
    )

    summaryItems = bpSummaryItems
  }

  override fun showNoBloodPressuresView() {
    summaryItems = emptyList()
  }

  override fun showSeeAllButton() {
    canShowSeeAllButton = true
  }

  override fun hideSeeAllButton() {
    canShowSeeAllButton = false
  }

  override fun openBloodPressureEntrySheet(patientUuid: UUID, currentFacility: Facility) {
    val bpEntrySheetIntent = BloodPressureEntrySheet.intentForNewBp(context, patientUuid)
    router.push(AlertFacilityChangeSheet.Key(
        currentFacilityName = currentFacility.name,
        continuation = ContinueToActivity(bpEntrySheetIntent, SUMMARY_REQCODE_BP_ENTRY)
    ))
  }

  override fun openBloodPressureUpdateSheet(bpUuid: UUID) {
    val intent = BloodPressureEntrySheet.intentForUpdateBp(context, bpUuid)
    activity.startActivity(intent)
  }

  override fun showBloodPressureHistoryScreen(patientUuid: UUID) {
    router.push(BloodPressureHistoryScreen.Key(patientUuid))
  }

  override fun registerSummaryModelUpdateCallback(callback: PatientSummaryModelUpdateCallback?) {
    modelUpdateCallback = callback
  }

  @SuppressLint("CheckResult")
  private fun setupBpRecordedEvents(screenDestroys: Observable<ScreenDestroyed>) {
    screenResults
        .streamResults()
        .ofType<ActivityResult>()
        .extractSuccessful(SUMMARY_REQCODE_BP_ENTRY) { intent ->
          BloodPressureEntrySheet.wasBloodPressureSaved(intent)
        }
        .takeUntil(screenDestroys)
        .subscribe { bpRecorded?.invoke() }
  }

  private fun generateBPSummaryItems(
      measurements: List<BloodPressureMeasurement>,
      canEditFor: Duration,
      utcClock: UtcClock,
      userClock: UserClock,
      dateFormatter: DateTimeFormatter,
      timeFormatter: DateTimeFormatter
  ): List<BloodPressureSummaryItem> {
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

        BloodPressureSummaryItem(
            id = measurement.uuid,
            systolic = measurement.reading.systolic,
            diastolic = measurement.reading.diastolic,
            date = dateFormatter.format(recordedAt),
            time = bpTime,
            isHigh = measurement.level.isHigh,
            canEdit = isBpEditable
        )
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
