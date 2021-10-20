package org.simple.clinic.home.overdue

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.f2prateek.rx.preferences2.Preference
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.Update
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.cast
import kotlinx.coroutines.rx2.asObservable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.databinding.ListItemOverduePatientBinding
import org.simple.clinic.databinding.ListItemOverduePlaceholderBinding
import org.simple.clinic.databinding.ScreenOverdueBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature.OverdueListDownloadAndShare
import org.simple.clinic.feature.Features
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.sync.LastSyncedState
import org.simple.clinic.sync.SyncProgress
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class OverdueScreen : BaseScreen<
    OverdueScreen.Key,
    ScreenOverdueBinding,
    OverdueModel,
    OverdueEvent,
    OverdueEffect,
    Unit>(), OverdueUiActions {

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var effectHandlerFactory: OverdueEffectHandler.Factory

  @Inject
  lateinit var lastSyncedState: Preference<LastSyncedState>

  private val overdueListAdapter = PagingItemAdapter(
      diffCallback = OverdueAppointmentListItem.DiffCallback(),
      bindings = mapOf(
          R.layout.list_item_overdue_patient to { layoutInflater, parent ->
            ListItemOverduePatientBinding.inflate(layoutInflater, parent, false)
          }
      ),
      placeHolderBinding = R.layout.list_item_overdue_placeholder to { layoutInflater, parent ->
        ListItemOverduePlaceholderBinding.inflate(layoutInflater, parent, false)
      }
  )

  private val disposable = CompositeDisposable()

  private val viewForEmptyList
    get() = binding.viewForEmptyList

  private val overdueRecyclerView
    get() = binding.overdueRecyclerView

  private val overdueProgressBar
    get() = binding.overdueProgressBar

  private val buttonsFrame
    get() = binding.buttonsFrame

  private val downloadOverdueListButton
    get() = binding.downloadOverdueListButton

  private val shareOverdueListButton
    get() = binding.shareOverdueListButton

  override fun defaultModel() = OverdueModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenOverdueBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable.mergeArray(
      overdueListAdapter.itemEvents,
      downloadOverdueListClicks(),
      shareOverdueListClicks()
  )
      .compose(ReportAnalyticsEvents())
      .share()
      .cast<OverdueEvent>()

  override fun createUpdate(): Update<OverdueModel, OverdueEvent, OverdueEffect> {
    val date = LocalDate.now(userClock)
    return OverdueUpdate(date)
  }

  override fun createInit() = OverdueInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    overdueRecyclerView.adapter = overdueListAdapter
    overdueRecyclerView.layoutManager = LinearLayoutManager(context)

    buttonsFrame.visibleOrGone(isVisible = features.isEnabled(OverdueListDownloadAndShare))

    disposable.add(overdueListLoadStateListener())
  }

  override fun onDestroyView() {
    overdueRecyclerView.adapter = null
    disposable.clear()
    super.onDestroyView()
  }

  override fun openPhoneMaskBottomSheet(patientUuid: UUID) {
    router.push(ContactPatientBottomSheet.Key(patientUuid))
  }

  override fun showOverdueAppointments(
      overdueAppointments: PagingData<OverdueAppointment>,
      isDiabetesManagementEnabled: Boolean
  ) {
    overdueListAdapter.submitData(lifecycle, OverdueAppointmentListItem.from(
        appointments = overdueAppointments,
        clock = userClock
    ))
  }

  override fun openPatientSummary(patientUuid: UUID) {
    router.push(
        PatientSummaryScreenKey(
            patientUuid = patientUuid,
            intention = OpenIntention.ViewExistingPatient,
            screenCreatedTimestamp = Instant.now(utcClock)
        )
    )
  }

  private fun downloadOverdueListClicks(): Observable<UiEvent> {
    return downloadOverdueListButton
        .clicks()
        .map { DownloadOverdueListClicked }
  }

  private fun shareOverdueListClicks(): Observable<UiEvent> {
    return shareOverdueListButton
        .clicks()
        .map { ShareOverdueListClicked }
  }

  private fun overdueListLoadStateListener(): Disposable {
    return Observables
        .combineLatest(
            lastSyncedState.asObservable(),
            overdueListAdapter.loadStateFlow.asObservable()
        )
        .subscribe { (syncState, loadStates) ->
          val isSyncingPatientData = syncState.lastSyncProgress == SyncProgress.SYNCING
          val isLoadingInitialData = loadStates.refresh is LoadState.Loading
          val isOverdueListLoading = isSyncingPatientData || isLoadingInitialData
          val hasNoAdapterItems = overdueListAdapter.itemCount == 0

          when {
            isOverdueListLoading && hasNoAdapterItems -> loadingOverdueList()
            else -> {
              val shouldShowEmptyView = loadStates.append.endOfPaginationReached && hasNoAdapterItems

              overdueListLoaded(shouldShowEmptyView)
            }
          }
        }
  }

  private fun overdueListLoaded(shouldShowEmptyView: Boolean) {
    overdueProgressBar.visibility = View.GONE
    viewForEmptyList.visibleOrGone(isVisible = shouldShowEmptyView)
    overdueRecyclerView.visibleOrGone(isVisible = !shouldShowEmptyView)

    (parentFragment as HomeScreen).overdueListCountUpdated(overdueListAdapter.itemCount)
  }

  private fun loadingOverdueList() {
    overdueProgressBar.visibility = View.VISIBLE
    viewForEmptyList.visibility = View.GONE
    overdueRecyclerView.visibility = View.GONE
  }

  interface Injector {
    fun inject(target: OverdueScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Overdue"
  ) : ScreenKey() {

    override fun instantiateFragment() = OverdueScreen()
  }
}
