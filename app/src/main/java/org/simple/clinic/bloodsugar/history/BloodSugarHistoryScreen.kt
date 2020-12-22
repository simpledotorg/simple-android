package org.simple.clinic.bloodsugar.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bloodsugar.BloodSugarHistoryListItemDataSourceFactory
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryItemClicked
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItemDiffCallback
import org.simple.clinic.bloodsugar.history.adapter.NewBloodSugarClicked
import org.simple.clinic.bloodsugar.selection.type.BloodSugarTypePickerSheet
import org.simple.clinic.databinding.ListBloodSugarHistoryItemBinding
import org.simple.clinic.databinding.ListNewBloodSugarButtonBinding
import org.simple.clinic.databinding.ScreenBloodSugarHistoryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.DateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.TYPE_PICKER_SHEET
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfig
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.dp
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class BloodSugarHistoryScreen(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs), BloodSugarHistoryScreenUi, BloodSugarHistoryScreenUiActions {

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var userClock: UserClock

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  @Named("time_for_measurement_history")
  lateinit var timeFormatter: DateTimeFormatter

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandlerFactory: BloodSugarHistoryScreenEffectHandler.Factory

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var config: BloodSugarSummaryConfig

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  @Named("for_measurement_history")
  lateinit var measurementHistoryPaginationConfig: PagedList.Config

  private var binding: ScreenBloodSugarHistoryBinding? = null

  private val toolbar
    get() = binding!!.toolbar

  private val bloodSugarHistoryList
    get() = binding!!.bloodSugarHistoryList

  private val bloodSugarHistoryAdapter = PagingItemAdapter(
      diffCallback = BloodSugarHistoryListItemDiffCallback(),
      bindings = mapOf(
          R.layout.list_new_blood_sugar_button to { layoutInflater, parent ->
            ListNewBloodSugarButtonBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_blood_sugar_history_item to { layoutInflater, parent ->
            ListBloodSugarHistoryItemBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val events: Observable<BloodSugarHistoryScreenEvent> by unsafeLazy {
    Observable
        .merge(
            addNewBloodSugarClicked(),
            bloodPressureClicked()
        )
        .compose(ReportAnalyticsEvents())
        .cast<BloodSugarHistoryScreenEvent>()
  }

  private val uiRenderer = BloodSugarHistoryScreenUiRenderer(this)

  private val delegate: MobiusDelegate<BloodSugarHistoryScreenModel, BloodSugarHistoryScreenEvent, BloodSugarHistoryScreenEffect> by unsafeLazy {
    val screenKey = screenRouter.key<BloodSugarHistoryScreenKey>(this)
    MobiusDelegate(
        events = events,
        defaultModel = BloodSugarHistoryScreenModel.create(screenKey.patientUuid),
        init = BloodSugarHistoryScreenInit(),
        update = BloodSugarHistoryScreenUpdate(),
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

    binding = ScreenBloodSugarHistoryBinding.bind(this)

    context.injector<BloodSugarHistoryScreenInjector>().inject(this)

    val screenDestroys: Observable<ScreenDestroyed> = detaches().map { ScreenDestroyed() }
    openEntrySheetAfterTypeIsSelected(screenDestroys)

    delegate.prepare()

    handleToolbarBackClick()
    setupBloodSugarHistoryList()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun showPatientInformation(patient: Patient) {
    val ageValue = DateOfBirth.fromPatient(patient, userClock).estimateAge(userClock)
    displayNameGenderAge(patient.fullName, patient.gender, ageValue)
  }

  override fun openBloodSugarEntrySheet(patientUuid: UUID) {
    val intent = BloodSugarTypePickerSheet.intent(context)
    activity.startActivityForResult(intent, TYPE_PICKER_SHEET)
  }

  override fun openBloodSugarUpdateSheet(measurement: BloodSugarMeasurement) {
    val intent = BloodSugarEntrySheet.intentForUpdateBloodSugar(context, measurement.uuid, measurement.reading.type)
    activity.startActivity(intent)
  }

  @SuppressLint("CheckResult")
  override fun showBloodSugars(dataSourceFactory: BloodSugarHistoryListItemDataSourceFactory) {
    val detaches = detaches()
    // Initial load size hint should be a multiple of page size
    dataSourceFactory.toObservable(config = measurementHistoryPaginationConfig, detaches = detaches)
        .takeUntil(detaches)
        .subscribe(bloodSugarHistoryAdapter::submitList)
  }

  private fun displayNameGenderAge(name: String, gender: Gender, age: Int) {
    val genderLetter = resources.getString(gender.displayLetterRes)
    toolbar.title = resources.getString(R.string.bloodsugarhistory_toolbar_title, name, genderLetter, age.toString())
  }

  private fun handleToolbarBackClick() {
    toolbar.setNavigationOnClickListener {
      screenRouter.pop()
    }
  }

  private fun setupBloodSugarHistoryList() {
    val dividerMargin = 8.dp
    val divider = DividerItemDecorator(context = context, marginStart = dividerMargin, marginEnd = dividerMargin)

    bloodSugarHistoryList.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context)
      addItemDecoration(divider)
      adapter = bloodSugarHistoryAdapter
    }
  }

  @SuppressLint("CheckResult")
  private fun openEntrySheetAfterTypeIsSelected(onDestroys: Observable<ScreenDestroyed>) {
    screenRouter.streamScreenResults()
        .ofType<ActivityResult>()
        .extractSuccessful(TYPE_PICKER_SHEET) { intent -> intent }
        .takeUntil(onDestroys)
        .subscribe(::showBloodSugarEntrySheet)
  }

  private fun showBloodSugarEntrySheet(intent: Intent) {
    val screenKey = screenRouter.key<BloodSugarHistoryScreenKey>(this)
    val patientUuid = screenKey.patientUuid

    val intentForNewBloodSugar = BloodSugarEntrySheet.intentForNewBloodSugar(
        context,
        patientUuid,
        BloodSugarTypePickerSheet.selectedBloodSugarType(intent)
    )
    activity.startActivity(intentForNewBloodSugar)
  }

  private fun addNewBloodSugarClicked(): Observable<BloodSugarHistoryScreenEvent> {
    return bloodSugarHistoryAdapter
        .itemEvents
        .ofType<NewBloodSugarClicked>()
        .map { AddNewBloodSugarClicked }
  }

  private fun bloodPressureClicked(): Observable<BloodSugarHistoryScreenEvent> {
    return bloodSugarHistoryAdapter
        .itemEvents
        .ofType<BloodSugarHistoryItemClicked>()
        .map { it.measurement }
        .map(::BloodSugarClicked)
  }
}
