package org.simple.clinic.bp.history

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.simple.clinic.bp.BloodPressureHistoryListItemDataSourceFactory
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItemDiffCallback
import org.simple.clinic.bp.history.adapter.Event.AddNewBpClicked
import org.simple.clinic.bp.history.adapter.Event.BloodPressureHistoryItemClicked
import org.simple.clinic.databinding.ListBpHistoryItemBinding
import org.simple.clinic.databinding.ListNewBpButtonBinding
import org.simple.clinic.databinding.ScreenBpHistoryBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.displayLetterRes
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.PagingItemAdapter_old
import org.simple.clinic.widgets.dp
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class BloodPressureHistoryScreen : BaseScreen<
    BloodPressureHistoryScreen.Key,
    ScreenBpHistoryBinding,
    BloodPressureHistoryScreenModel,
    BloodPressureHistoryScreenEvent,
    BloodPressureHistoryScreenEffect,
    BloodPressureHistoryViewEffect>(), BloodPressureHistoryScreenUi, BloodPressureHistoryScreenUiActions {

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var config: PatientSummaryConfig

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandler: BloodPressureHistoryScreenEffectHandler.Factory

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  @Named("time_for_measurement_history")
  lateinit var timeFormatter: DateTimeFormatter

  @Inject
  @Named("for_measurement_history")
  lateinit var measurementHistoryPaginationConfig: PagedList.Config

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  private val bloodPressureHistoryAdapter = PagingItemAdapter_old(
      diffCallback = BloodPressureHistoryListItemDiffCallback(),
      bindings = mapOf(
          R.layout.list_new_bp_button to { layoutInflater, parent ->
            ListNewBpButtonBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_bp_history_item to { layoutInflater, parent ->
            ListBpHistoryItemBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val bpHistoryList
    get() = binding.bpHistoryList

  private val toolbar
    get() = binding.toolbar

  private val disposable = CompositeDisposable()

  override fun events() = Observable
      .merge(
          addNewBpClicked(),
          bloodPressureClicked()
      )
      .compose(ReportAnalyticsEvents())
      .cast<BloodPressureHistoryScreenEvent>()

  override fun defaultModel() = BloodPressureHistoryScreenModel.create(screenKey.patientId)

  override fun createInit() = BloodPressureHistoryScreenInit()

  override fun createUpdate() = BloodPressureHistoryScreenUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<BloodPressureHistoryViewEffect>) = effectHandler
      .create(viewEffectsConsumer = viewEffectsConsumer)
      .build()

  override fun uiRenderer() = BloodPressureHistoryScreenUiRenderer(this)

  override fun viewEffectHandler() = BloodPressureHistoryViewEffectHandler(uiActions = this)

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenBpHistoryBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupBloodPressureHistoryList()
    handleToolbarBackClick()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    disposable.dispose()
  }

  private fun setupBloodPressureHistoryList() {
    val dividerMargin = 8.dp
    val divider = DividerItemDecorator(context = requireContext(), marginStart = dividerMargin, marginEnd = dividerMargin)

    bpHistoryList.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(requireContext())
      addItemDecoration(divider)
      adapter = bloodPressureHistoryAdapter
    }
  }

  private fun handleToolbarBackClick() {
    toolbar.setNavigationOnClickListener {
      router.pop()
    }
  }

  override fun showPatientInformation(patient: Patient) {
    val ageValue = patient.ageDetails.estimateAge(userClock)
    displayNameGenderAge(patient.fullName, patient.gender, ageValue)
  }

  override fun openBloodPressureEntrySheet(patientUuid: UUID) {
    val intent = BloodPressureEntrySheet.intentForNewBp(requireContext(), patientUuid)
    requireContext().startActivity(intent)
  }

  override fun openBloodPressureUpdateSheet(bpUuid: UUID, patientUuid: UUID) {
    val intent = BloodPressureEntrySheet.intentForUpdateBp(requireContext(), bpUuid, patientUuid)
    requireContext().startActivity(intent)
  }

  override fun showBloodPressures(dataSourceFactory: BloodPressureHistoryListItemDataSourceFactory) {
    // TODO: Remove this once Paging 3 implementation is added for blood pressure history.
    val detaches = viewLifecycleOwnerLiveData
        .asFlow()
        .mapNotNull { it }
        .asObservable()
        .filter { it.lifecycle.currentState == Lifecycle.State.DESTROYED }
        .map { Unit }

    // Initial load size hint should be a multiple of page size
    disposable.add(dataSourceFactory.toObservable(config = measurementHistoryPaginationConfig, detaches = detaches)
        .subscribe(bloodPressureHistoryAdapter::submitList))
  }

  private fun displayNameGenderAge(name: String, gender: Gender, age: Int) {
    val genderLetter = resources.getString(gender.displayLetterRes)
    toolbar.title = resources.getString(R.string.bloodpressurehistory_toolbar_title, name, genderLetter, age.toString())
  }

  private fun addNewBpClicked(): Observable<BloodPressureHistoryScreenEvent> {
    return bloodPressureHistoryAdapter
        .itemEvents
        .ofType<AddNewBpClicked>()
        .map { NewBloodPressureClicked }
  }

  private fun bloodPressureClicked(): Observable<BloodPressureHistoryScreenEvent> {
    return bloodPressureHistoryAdapter
        .itemEvents
        .ofType<BloodPressureHistoryItemClicked>()
        .map { it.measurement }
        .map(::BloodPressureClicked)
  }

  @Parcelize
  data class Key(
      val patientId: UUID,
      override val analyticsName: String = "Blood Pressure History"
  ) : ScreenKey() {

    override fun instantiateFragment() = BloodPressureHistoryScreen()
  }

  interface Injector {
    fun inject(target: BloodPressureHistoryScreen)
  }
}
