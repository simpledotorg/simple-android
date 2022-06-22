package org.simple.clinic.home.overdue.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.jakewharton.rxbinding3.widget.itemClicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.databinding.ListItemOverduePatientBinding
import org.simple.clinic.databinding.ListItemOverduePlaceholderBinding
import org.simple.clinic.databinding.ScreenOverdueSearchBinding
import org.simple.clinic.di.injector
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.afterTextChangedWatcher
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setTextWithWatcher
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class OverdueSearchScreen : BaseScreen<
    OverdueSearchScreen.Key,
    ScreenOverdueSearchBinding,
    OverdueSearchModel,
    OverdueSearchEvent,
    OverdueSearchEffect,
    OverdueSearchViewEffect>(), OverdueSearchUi, OverdueSearchUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var effectHandlerFactory: OverdueSearchEffectHandler.Factory

  private val overdueSearchToolbar
    get() = binding.overdueSearchToolbar

  private val overdueSearchRecyclerView
    get() = binding.overdueSearchResults

  private val overdueSearchProgressIndicator
    get() = binding.overdueSearchProgressIndicator

  private val noOverdueSearchResultsContainer
    get() = binding.noOverdueSearchResultsContainer

  private val overdueSearchHistoryContainer
    get() = binding.overdueSearchHistoryContainer

  private val overdueSearchQueryEditText
    get() = binding.overdueSearchQueryEditText

  private val hotEvents = PublishSubject.create<UiEvent>()

  private val disposable = CompositeDisposable()

  private val searchQueryTextChanges by unsafeLazy {
    afterTextChangedWatcher { text ->
      if (text != null)
        hotEvents.onNext(OverdueSearchQueryChanged(text.trim().toString()))
    }
  }

  private val overdueSearchListAdapter = PagingItemAdapter(
      diffCallback = OverdueAppointmentSearchListItem.DiffCallback(),
      bindings = mapOf(
          R.layout.list_item_overdue_patient to { layoutInflater, parent ->
            ListItemOverduePatientBinding.inflate(layoutInflater, parent, false)
          }
      ),
      placeHolderBinding = R.layout.list_item_overdue_placeholder to { layoutInflater, parent ->
        ListItemOverduePlaceholderBinding.inflate(layoutInflater, parent, false)
      }
  )

  private val searchHistoryAdapter by unsafeLazy {
    ArrayAdapter<String>(
        requireContext(),
        R.layout.view_overdue_search_history,
        R.id.search_history_text,
        mutableListOf()
    )
  }

  override fun defaultModel() = OverdueSearchModel.create()

  override fun createUpdate() = OverdueSearchUpdate(LocalDate.now(userClock))

  override fun createInit() = OverdueSearchInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<OverdueSearchViewEffect>) = effectHandlerFactory
      .create(viewEffectsConsumer)
      .build()

  override fun uiRenderer() = OverdueSearchUiRenderer(this)

  override fun viewEffectHandler() = OverdueSearchViewEffectHandler(this)

  override fun events() = Observable
      .mergeArray(
          overdueSearchListAdapter.itemEvents,
          searchHistoryItemClicks(),
          hotEvents
      )
      .compose(ReportAnalyticsEvents())
      .cast<OverdueSearchEvent>()

  private fun searchHistoryItemClicks(): Observable<UiEvent> {
    return overdueSearchHistoryContainer
        .itemClicks()
        .map {
          val searchQuery = searchHistoryAdapter.getItem(it)

          OverdueSearchHistoryClicked(searchQuery!!)
        }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?): ScreenOverdueSearchBinding {
    return ScreenOverdueSearchBinding.inflate(layoutInflater, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    overdueSearchToolbar.setNavigationOnClickListener {
      router.pop()
    }

    overdueSearchRecyclerView.adapter = overdueSearchListAdapter
    overdueSearchHistoryContainer.adapter = searchHistoryAdapter
    disposable.add(overdueSearchResultsLoadStateListener())
  }

  private fun overdueSearchResultsLoadStateListener() = overdueSearchListAdapter
      .loadStateFlow
      .asObservable()
      .subscribe { combinedLoadStates ->
        val isLoadingInitialData = combinedLoadStates.refresh is LoadState.Loading
        val hasNoAdapterItems = overdueSearchListAdapter.itemCount == 0

        when {
          isLoadingInitialData && hasNoAdapterItems -> loadingOverdueSearchResults()
          else -> {
            val shouldShowEmptyView = combinedLoadStates.append.endOfPaginationReached && hasNoAdapterItems

            overdueSearchResultsLoaded(shouldShowEmptyView)
          }
        }
      }

  private fun overdueSearchResultsLoaded(shouldShowEmptyView: Boolean) {
    overdueSearchProgressIndicator.visibility = View.GONE
    noOverdueSearchResultsContainer.visibleOrGone(isVisible = shouldShowEmptyView)
    overdueSearchRecyclerView.visibleOrGone(isVisible = !shouldShowEmptyView)
  }

  private fun loadingOverdueSearchResults() {
    overdueSearchProgressIndicator.visibility = View.VISIBLE
    noOverdueSearchResultsContainer.visibility = View.GONE
    overdueSearchRecyclerView.visibility = View.GONE
  }

  override fun showSearchHistory(searchHistory: Set<String>) {
    overdueSearchHistoryContainer.visibility = View.VISIBLE
    searchHistoryAdapter.clear()
    searchHistoryAdapter.addAll(searchHistory)
  }

  override fun hideSearchResults() {
    overdueSearchRecyclerView.visibility = View.GONE
  }

  override fun showOverdueSearchResults(searchResults: PagingData<OverdueAppointment>) {
    overdueSearchRecyclerView.visibility = View.VISIBLE
    overdueSearchListAdapter.submitData(
        lifecycle,
        OverdueAppointmentSearchListItem.from(searchResults, userClock)
    )
  }

  override fun hideSearchHistory() {
    overdueSearchHistoryContainer.visibility = View.GONE
  }

  override fun renderSearchQuery(searchQuery: String) {
    overdueSearchQueryEditText.setTextWithWatcher(searchQuery, searchQueryTextChanges)
  }

  override fun openPatientSummaryScreen(patientUuid: UUID) {
    router.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewExistingPatient, Instant.now(userClock)))
  }

  override fun openContactPatientSheet(patientUuid: UUID) {
    router.push(ContactPatientBottomSheet.Key(patientUuid))
  }

  interface Injector {
    fun inject(target: OverdueSearchScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Overdue Search Screen"
  ) : ScreenKey() {

    override fun instantiateFragment(): Fragment {
      return OverdueSearchScreen()
    }
  }
}
