package org.simple.clinic.home.overdue

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.Update
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.databinding.ItemOverdueListPatientBinding
import org.simple.clinic.databinding.ScreenOverdueBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Feature.OverdueListChanges
import org.simple.clinic.feature.Features
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
    OverdueEffect>(), OverdueUiActions {

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
      diffCallback = OverdueAppointmentRow.DiffCallback(),
      bindings = mapOf(
          R.layout.item_overdue_list_patient to { layoutInflater, parent ->
            ItemOverdueListPatientBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val viewForEmptyList
    get() = binding.viewForEmptyList

  private val overdueRecyclerView
    get() = binding.overdueRecyclerView

  private val overdueProgressBar
    get() = binding.overdueProgressBar

  private val screenDestroys = PublishSubject.create<Unit>()

  override fun defaultModel() = OverdueModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenOverdueBinding.inflate(layoutInflater, container, false)

  override fun events() = overdueListAdapter
      .itemEvents
      .compose(ReportAnalyticsEvents())
      .share()
      .cast<OverdueEvent>()

  override fun createUpdate(): Update<OverdueModel, OverdueEvent, OverdueEffect> {
    val date = LocalDate.now(userClock)
    return OverdueUpdate(date, features.isEnabled(OverdueListChanges))
  }

  override fun createInit() = OverdueInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    overdueRecyclerView.adapter = overdueListAdapter
    overdueRecyclerView.layoutManager = LinearLayoutManager(context)

    overdueListAdapter.addLoadStateListener(::overdueListAdapterLoadStateListener)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    overdueListAdapter.removeLoadStateListener(::overdueListAdapterLoadStateListener)
    screenDestroys.onNext(Unit)
  }

  override fun openPhoneMaskBottomSheet(patientUuid: UUID) {
    router.push(ContactPatientBottomSheet.Key(patientUuid))
  }

  override fun showOverdueAppointments(
      overdueAppointments: PagingData<OverdueAppointment>,
      isDiabetesManagementEnabled: Boolean
  ) {
    overdueListAdapter.submitData(lifecycle, OverdueAppointmentRow.from(
        appointments = overdueAppointments,
        clock = userClock,
        dateFormatter = dateFormatter,
        isDiabetesManagementEnabled = isDiabetesManagementEnabled
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

  private fun overdueListAdapterLoadStateListener(loadStates: CombinedLoadStates) {
    val isSyncingPatientData = lastSyncedState.get().lastSyncProgress == SyncProgress.SYNCING
    val isLoading = loadStates.refresh is LoadState.Loading
    val endOfPaginationReached = loadStates.append.endOfPaginationReached
    val hasNoAdapterItems = overdueListAdapter.itemCount == 0

    val shouldShowEmptyView = endOfPaginationReached && hasNoAdapterItems

    overdueProgressBar.visibleOrGone(isVisible = (isLoading || isSyncingPatientData) && hasNoAdapterItems)
    viewForEmptyList.visibleOrGone(isVisible = shouldShowEmptyView && !isLoading && !isSyncingPatientData)
    overdueRecyclerView.visibleOrGone(isVisible = !shouldShowEmptyView)
  }

  interface Injector {
    fun inject(target: OverdueScreen)
  }

  @Parcelize
  class Key : ScreenKey() {

    override val analyticsName = "Overdue"

    override fun instantiateFragment() = OverdueScreen()
  }
}
