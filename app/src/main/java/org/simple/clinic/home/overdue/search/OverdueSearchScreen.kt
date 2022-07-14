package org.simple.clinic.home.overdue.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.recyclerview.scrollStateChanges
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.itemClicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.appconfig.Country
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.databinding.ListItemOverduePatientBinding
import org.simple.clinic.databinding.ListItemOverduePlaceholderBinding
import org.simple.clinic.databinding.ScreenOverdueSearchBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.NO_RESULTS
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit
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
  lateinit var features: Features

  @Inject
  lateinit var country: Country

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

  private val downloadAndShareButtonFrame
    get() = binding.downloadAndShareButtonFrame

  private val selectedOverdueCountView
    get() = binding.selectedOverdueCountView

  private val selectedOverdueAppointmentsCountTextView
    get() = binding.selectedOverdueAppointmentsTextView

  private val clearSelectedOverdueAppointmentsButton
    get() = binding.clearSelectedOverdueAppointmentsButton

  private val hotEvents = PublishSubject.create<UiEvent>()

  private val disposable = CompositeDisposable()

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
      .create(
          viewEffectsConsumer = viewEffectsConsumer,
          pagingCacheScope = viewLifecycleOwner.lifecycleScope
      )
      .build()

  override fun uiRenderer() = OverdueSearchUiRenderer(this)

  override fun viewEffectHandler() = OverdueSearchViewEffectHandler(this)

  override fun events() = Observable
      .mergeArray(
          overdueSearchListAdapter.itemEvents,
          searchHistoryItemClicks(),
          hotEvents,
          overdueSearchQueryTextChanges(),
          clearSelectedOverdueAppointmentClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<OverdueSearchEvent>()

  private fun clearSelectedOverdueAppointmentClicks() = clearSelectedOverdueAppointmentsButton
      .clicks()
      .map { ClearSelectedOverdueAppointments }

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

  override fun onStart() {
    super.onStart()
    hotEvents.onNext(OverdueSearchScreenShown)
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

    overdueSearchQueryEditText.showKeyboard()

    disposable.addAll(
        hideKeyboardOnSearchResultsScroll(),
        hideKeyboardOnImeAction(),
        overdueSearchResultsLoadStateListener()
    )
  }

  private fun overdueSearchResultsLoadStateListener() = overdueSearchListAdapter
      .loadStateFlow
      .asObservable()
      .subscribe { combinedLoadStates ->
        val isLoadingData = combinedLoadStates.refresh is LoadState.Loading
        val endOfPaginationReached = combinedLoadStates.append.endOfPaginationReached
        val hasNoAdapterItems = overdueSearchListAdapter.itemCount == 0
        val showNoSearchResults = !isLoadingData && endOfPaginationReached && hasNoAdapterItems

        when {
          isLoadingData -> hotEvents.onNext(OverdueSearchLoadStateChanged(IN_PROGRESS))
          showNoSearchResults -> hotEvents.onNext(OverdueSearchLoadStateChanged(NO_RESULTS))
          else -> hotEvents.onNext(OverdueSearchLoadStateChanged(DONE))
        }
      }

  override fun showSearchHistory(searchHistory: Set<String>) {
    overdueSearchHistoryContainer.visibility = VISIBLE
    searchHistoryAdapter.clear()
    searchHistoryAdapter.addAll(searchHistory)
  }

  override fun showSearchResults() {
    overdueSearchRecyclerView.visibility = VISIBLE
  }

  override fun hideSearchResults() {
    overdueSearchListAdapter.submitData(lifecycle, PagingData.empty())
    overdueSearchRecyclerView.visibility = GONE
  }

  override fun setOverdueSearchResultsPagingData(
      overdueSearchResults: PagingData<OverdueAppointment>,
      selectedOverdueAppointments: Set<UUID>,
      searchQuery: String
  ) {
    overdueSearchListAdapter.submitData(
        lifecycle,
        OverdueAppointmentSearchListItem.from(
            appointments = overdueSearchResults,
            selectedOverdueAppointments = selectedOverdueAppointments,
            clock = userClock,
            searchQuery = searchQuery,
            isOverdueSelectAndDownloadEnabled = features.isEnabled(Feature.OverdueSelectAndDownload) && country.isoCountryCode == Country.INDIA
        )
    )
  }

  override fun showDownloadAndShareButtons() {
    downloadAndShareButtonFrame.visibility = VISIBLE
  }

  override fun hideDownloadAndShareButtons() {
    downloadAndShareButtonFrame.visibility = GONE
  }

  override fun showSelectedOverdueAppointmentCount(selectedOverdueAppointments: Int) {
    selectedOverdueCountView.visibility = VISIBLE
    selectedOverdueAppointmentsCountTextView.text = getString(R.string.selected_overdue_count, selectedOverdueAppointments)
  }

  override fun hideSelectedOverdueAppointmentCount() {
    selectedOverdueCountView.visibility = GONE
  }

  private fun hideKeyboardOnSearchResultsScroll(): Disposable {
    return overdueSearchRecyclerView
        .scrollStateChanges()
        .filter { it == RecyclerView.SCROLL_STATE_DRAGGING }
        .subscribe { hideKeyboard() }
  }

  private fun hideKeyboardOnImeAction(): Disposable {
    return overdueSearchQueryEditText
        .editorActions { actionId -> actionId == EditorInfo.IME_ACTION_SEARCH }
        .subscribe { hideKeyboard() }
  }

  fun hideKeyboard() {
    binding.root.hideKeyboard()
  }

  override fun hideSearchHistory() {
    overdueSearchHistoryContainer.visibility = GONE
  }

  override fun showProgress() {
    overdueSearchProgressIndicator.visibility = VISIBLE
  }

  override fun hideProgress() {
    overdueSearchProgressIndicator.visibility = GONE
  }

  override fun showNoSearchResults() {
    noOverdueSearchResultsContainer.visibility = VISIBLE
  }

  override fun hideNoSearchResults() {
    noOverdueSearchResultsContainer.visibility = GONE
  }

  override fun openPatientSummaryScreen(patientUuid: UUID) {
    router.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewExistingPatient, Instant.now(userClock)))
  }

  override fun openContactPatientSheet(patientUuid: UUID) {
    router.push(ContactPatientBottomSheet.Key(patientUuid))
  }

  override fun setOverdueSearchQuery(searchQuery: String) {
    overdueSearchQueryEditText.setTextAndCursor(searchQuery)
  }

  private fun overdueSearchQueryTextChanges(): Observable<UiEvent> {
    return overdueSearchQueryEditText
        .textChanges()
        .skipInitialValue()
        .debounce(500, TimeUnit.MILLISECONDS)
        .map {
          OverdueSearchQueryChanged(it.toString())
        }
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
