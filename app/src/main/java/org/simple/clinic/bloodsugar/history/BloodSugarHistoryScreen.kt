package org.simple.clinic.bloodsugar.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.rx2.asObservable
import kotlinx.parcelize.Parcelize
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
import org.simple.clinic.navigation.v2.ActivityResult
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.summary.TYPE_PICKER_SHEET
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryConfig
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.PagingItemAdapter_old
import org.simple.clinic.widgets.dp
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class BloodSugarHistoryScreen : BaseScreen<
    BloodSugarHistoryScreen.Key,
    ScreenBloodSugarHistoryBinding,
    BloodSugarHistoryScreenModel,
    BloodSugarHistoryScreenEvent,
    BloodSugarHistoryScreenEffect,
    BloodSugarHistoryScreenViewEffect>(), BloodSugarHistoryScreenUi, BloodSugarHistoryScreenUiActions {

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
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var effectHandlerFactory: BloodSugarHistoryScreenEffectHandler.Factory

  @Inject
  lateinit var config: BloodSugarSummaryConfig

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  @Named("for_measurement_history")
  lateinit var measurementHistoryPaginationConfig: PagedList.Config

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private val toolbar
    get() = binding.toolbar

  private val bloodSugarHistoryList
    get() = binding.bloodSugarHistoryList

  private val bloodSugarHistoryAdapter = PagingItemAdapter_old(
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

  private val disposable = CompositeDisposable()

  override fun defaultModel() = BloodSugarHistoryScreenModel.create(screenKey.patientId)

  override fun createInit() = BloodSugarHistoryScreenInit()

  override fun createUpdate() = BloodSugarHistoryScreenUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<BloodSugarHistoryScreenViewEffect>) = effectHandlerFactory.create(
      viewEffectsConsumer
  ).build()

  override fun viewEffectHandler() = BloodSugarHistoryScreenViewEffectHandler(this)

  override fun uiRenderer() = BloodSugarHistoryScreenUiRenderer(this)

  override fun events() = Observable
      .merge(
          addNewBloodSugarClicked(),
          bloodPressureClicked()
      )
      .compose(ReportAnalyticsEvents())
      .cast<BloodSugarHistoryScreenEvent>()

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenBloodSugarHistoryBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    openEntrySheetAfterTypeIsSelected()
    handleToolbarBackClick()
    setupBloodSugarHistoryList()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    disposable.dispose()
    disposable.clear()
  }

  override fun showPatientInformation(patient: Patient) {
    val ageValue = patient.ageDetails.estimateAge(userClock)
    displayNameGenderAge(patient.fullName, patient.gender, ageValue)
  }

  override fun openBloodSugarEntrySheet(patientUuid: UUID) {
    val intent = BloodSugarTypePickerSheet.intent(requireContext())
    activity.startActivityForResult(intent, TYPE_PICKER_SHEET)
  }

  override fun openBloodSugarUpdateSheet(measurement: BloodSugarMeasurement) {
    val intent = BloodSugarEntrySheet.intentForUpdateBloodSugar(requireContext(), measurement.uuid, measurement.reading.type)
    activity.startActivity(intent)
  }

  override fun showBloodSugars(dataSourceFactory: BloodSugarHistoryListItemDataSourceFactory) {
    // TODO: Remove this once Paging 3 implementation is added for blood sugar history.
    val detaches = viewLifecycleOwnerLiveData
        .asFlow()
        .mapNotNull { it }
        .asObservable()
        .filter { it.lifecycle.currentState == Lifecycle.State.DESTROYED }
        .map { Unit }

    // Initial load size hint should be a multiple of page size
    disposable.add(dataSourceFactory.toObservable(config = measurementHistoryPaginationConfig, detaches = detaches)
        .subscribe(bloodSugarHistoryAdapter::submitList))
  }

  private fun displayNameGenderAge(name: String, gender: Gender, age: Int) {
    val genderLetter = resources.getString(gender.displayLetterRes)
    toolbar.title = resources.getString(R.string.bloodsugarhistory_toolbar_title, name, genderLetter, age.toString())
  }

  private fun handleToolbarBackClick() {
    toolbar.setNavigationOnClickListener {
      router.pop()
    }
  }

  private fun setupBloodSugarHistoryList() {
    val dividerMargin = 8.dp
    val divider = DividerItemDecorator(context = requireContext(), marginStart = dividerMargin, marginEnd = dividerMargin)

    bloodSugarHistoryList.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(requireContext())
      addItemDecoration(divider)
      adapter = bloodSugarHistoryAdapter
    }
  }

  private fun openEntrySheetAfterTypeIsSelected() {
    disposable.add(screenResults
        .streamResults()
        .ofType<ActivityResult>()
        .extractSuccessful(TYPE_PICKER_SHEET) { intent -> intent }
        .subscribe(::showBloodSugarEntrySheet))
  }

  private fun showBloodSugarEntrySheet(intent: Intent) {
    val patientUuid = screenKey.patientId

    val intentForNewBloodSugar = BloodSugarEntrySheet.intentForNewBloodSugar(
        requireContext(),
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

  @Parcelize
  data class Key(
      val patientId: UUID,
      override val analyticsName: String = "Blood Sugar History"
  ) : ScreenKey() {

    override fun instantiateFragment() = BloodSugarHistoryScreen()
  }

  interface Injector {
    fun inject(target: BloodSugarHistoryScreen)
  }
}
