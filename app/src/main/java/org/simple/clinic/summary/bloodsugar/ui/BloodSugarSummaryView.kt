package org.simple.clinic.summary.bloodsugar.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.f2prateek.rx.preferences2.Preference
import com.google.android.material.card.MaterialCardView
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.Unknown
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet
import org.simple.clinic.bloodsugar.history.BloodSugarHistoryScreen
import org.simple.clinic.bloodsugar.selection.type.BloodSugarTypePickerSheet
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation.ContinueToActivity
import org.simple.clinic.feature.Feature.EditBloodSugar
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.ActivityResult
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.summary.PatientSummaryChildView
import org.simple.clinic.summary.PatientSummaryModelUpdateCallback
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.summary.TYPE_PICKER_SHEET
import org.simple.clinic.summary.bloodsugar.BloodSugarClicked
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfig
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryViewEffect
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryViewEffectHandler
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryViewEvent
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryViewInit
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryViewModel
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryViewUi
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryViewUiRenderer
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryViewUpdate
import org.simple.clinic.summary.bloodsugar.NewBloodSugarClicked
import org.simple.clinic.summary.bloodsugar.SeeAllClicked
import org.simple.clinic.summary.bloodsugar.UiActions
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class BloodSugarSummaryView(
    context: Context,
    attributes: AttributeSet
) : MaterialCardView(context, attributes), BloodSugarSummaryViewUi, UiActions, PatientSummaryChildView {

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var timestampGenerator: RelativeTimestampGenerator

  @Inject
  lateinit var bloodSugarSummaryConfig: BloodSugarSummaryConfig

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var zoneId: ZoneId

  @Inject
  lateinit var effectHandlerFactory: BloodSugarSummaryViewEffectHandler.Factory

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  @Named("time_for_measurement_history")
  lateinit var timeFormatter: DateTimeFormatter

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var bloodSugarUnitPreference: Preference<BloodSugarUnitPreference>

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private val uiRenderer: BloodSugarSummaryViewUiRenderer by unsafeLazy {
    BloodSugarSummaryViewUiRenderer(this, bloodSugarSummaryConfig)
  }

  private val screenKey by unsafeLazy {
    screenKeyProvider.keyFor<PatientSummaryScreenKey>(this)
  }

  private val viewEvents = PublishSubject.create<BloodSugarSummaryViewEvent>()

  private var summaryItems by mutableStateOf(emptyList<BloodSugarSummaryItem>())
  private var canShowSeeAllButton by mutableStateOf(false)

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  private val events: Observable<BloodSugarSummaryViewEvent> by unsafeLazy {
    viewEvents
        .compose(ReportAnalyticsEvents())
        .cast<BloodSugarSummaryViewEvent>()
  }

  private val delegate: MobiusDelegate<BloodSugarSummaryViewModel, BloodSugarSummaryViewEvent, BloodSugarSummaryViewEffect> by unsafeLazy {
    MobiusDelegate.forView(
        events = events,
        defaultModel = BloodSugarSummaryViewModel.create(screenKey.patientUuid),
        init = BloodSugarSummaryViewInit(),
        update = BloodSugarSummaryViewUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = { model ->
          modelUpdateCallback?.invoke(model)
          uiRenderer.render(model)
        }
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<BloodSugarSummaryViewInjector>().inject(this)

    val screenDestroys: Observable<ScreenDestroyed> = detaches().map { ScreenDestroyed }
    openEntrySheetAfterTypeIsSelected(screenDestroys)

    addView(ComposeView(context).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

      setContent {
        SimpleTheme {
          BloodSugarSummary(
              summaryItems = summaryItems,
              canShowSeeAllButton = canShowSeeAllButton,
              onSeeAllClick = {
                viewEvents.onNext(SeeAllClicked)
              },
              onAddBPClick = {
                viewEvents.onNext(NewBloodSugarClicked)
              },
              onEditBPClick = { id, type ->
                viewEvents.onNext(BloodSugarClicked(id = id, measurementType = type))
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

  override fun showBloodSugarSummary(bloodSugars: List<BloodSugarMeasurement>) {
    summaryItems = generateBloodSugarSummaryItems(bloodSugars)
  }

  override fun showNoBloodSugarsView() {
    summaryItems = emptyList()
  }

  override fun showBloodSugarTypeSelector(currentFacility: Facility) {
    val intent = BloodSugarTypePickerSheet.intent(context)
    router.push(AlertFacilityChangeSheet.Key(
        currentFacilityName = currentFacility.name,
        continuation = ContinueToActivity(intent, TYPE_PICKER_SHEET)
    ))
  }

  override fun showSeeAllButton() {
    canShowSeeAllButton = true
  }

  override fun hideSeeAllButton() {
    canShowSeeAllButton = false
  }

  override fun showBloodSugarHistoryScreen(patientUuid: UUID) {
    router.push(BloodSugarHistoryScreen.Key(patientUuid))
  }

  override fun openBloodSugarUpdateSheet(
      bloodSugarMeasurementUuid: UUID,
      measurementType: BloodSugarMeasurementType
  ) {
    router.push(BloodSugarEntrySheet.Key.forUpdateBloodSugar(
        bloodSugarMeasurementUuid = bloodSugarMeasurementUuid,
        measurementType = measurementType
    ))
  }

  override fun registerSummaryModelUpdateCallback(callback: PatientSummaryModelUpdateCallback?) {
    modelUpdateCallback = callback
  }

  @SuppressLint("CheckResult")
  private fun openEntrySheetAfterTypeIsSelected(onDestroys: Observable<ScreenDestroyed>) {
    screenResults
        .streamResults()
        .ofType<ActivityResult>()
        .filter { it.requestCode == TYPE_PICKER_SHEET && it.succeeded() && it.data != null }
        .takeUntil(onDestroys)
        .map { it.data!! }
        .subscribe(::showBloodSugarEntrySheet)
  }

  private fun showBloodSugarEntrySheet(intent: Intent) {
    val patientUuid = screenKey.patientUuid
    router.push(BloodSugarEntrySheet.Key.forNewBloodSugar(
        patientUuid = patientUuid,
        measurementType = BloodSugarTypePickerSheet.selectedBloodSugarType(intent)
    ))
  }

  private fun generateBloodSugarSummaryItems(bloodSugarMeasurements: List<BloodSugarMeasurement>): List<BloodSugarSummaryItem> {
    val measurementsByDate = bloodSugarMeasurements.groupBy { item -> item.recordedAt.atZone(utcClock.zone).toLocalDate() }

    return measurementsByDate.mapValues { (_, measurementList) ->
      val hasMultipleMeasurementsInSameDate = measurementList.size > 1
      measurementList.map { measurement ->
        val isBloodSugarEditable = isBloodSugarEditable(measurement)
        val recordedAt = measurement.recordedAt.toLocalDateAtZone(userClock.zone)
        val bloodSugarTime = if (hasMultipleMeasurementsInSameDate) {
          timeFormatter.format(measurement.recordedAt.atZone(userClock.zone))
        } else {
          null
        }

        BloodSugarSummaryItem(
            id = measurement.uuid,
            reading = measurement.reading,
            measurementUnit = bloodSugarUnitPreference.get(),
            date = dateFormatter.format(recordedAt),
            time = bloodSugarTime,
            canEdit = isBloodSugarEditable
        )
      }
    }.values.flatten()
  }

  private fun isBloodSugarEditable(measurement: BloodSugarMeasurement): Boolean {
    if (features.isDisabled(EditBloodSugar) || measurement.reading.type is Unknown) {
      return false
    }
    val now = Instant.now(utcClock)
    val createdAt = measurement.timestamps.createdAt

    val durationSinceBloodSugarCreated = Duration.between(createdAt, now)
    return durationSinceBloodSugarCreated <= bloodSugarSummaryConfig.bloodSugarEditableDuration
  }
}
