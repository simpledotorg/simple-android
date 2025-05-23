package org.simple.clinic.home.overdue.search

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.recyclerview.scrollStateChanges
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.activity.permissions.RequestPermissions
import org.simple.clinic.activity.permissions.RuntimePermissions
import org.simple.clinic.appconfig.Country
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.databinding.ListItemOverduePatientBinding
import org.simple.clinic.databinding.ListItemOverduePlaceholderBinding
import org.simple.clinic.databinding.ListItemSearchOverdueSelectAllButtonBinding
import org.simple.clinic.databinding.ScreenOverdueSearchBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Features
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.NO_RESULTS
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.ScreenResultBus
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.overdue.download.formatdialog.Download
import org.simple.clinic.overdue.download.formatdialog.SelectOverdueDownloadFormatDialog
import org.simple.clinic.overdue.download.formatdialog.Share
import org.simple.clinic.overdue.download.formatdialog.SharingInProgress
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.RuntimeNetworkStatus
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.applyInsetsBottomPadding
import org.simple.clinic.util.applyStatusBarPadding
import org.simple.clinic.util.lightStatusBar
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
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
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var runtimeNetworkStatus: RuntimeNetworkStatus<UiEvent>

  @Inject
  lateinit var effectHandlerFactory: OverdueSearchEffectHandler.Factory

  private val appbar
    get() = binding.overdueSearchAppBar

  private val overdueSearchToolbar
    get() = binding.overdueSearchToolbar

  private val overdueSearchRecyclerView
    get() = binding.overdueSearchResults

  private val overdueSearchProgressIndicator
    get() = binding.overdueSearchProgressIndicator

  private val noOverdueSearchResultsContainer
    get() = binding.noOverdueSearchResultsContainer

  private val downloadAndShareButtonFrame
    get() = binding.downloadAndShareButtonFrame

  private val selectedOverdueCountView
    get() = binding.selectedOverdueCountView

  private val selectedOverdueAppointmentsCountTextView
    get() = binding.selectedOverdueAppointmentsTextView

  private val clearSelectedOverdueAppointmentsButton
    get() = binding.clearSelectedOverdueAppointmentsButton

  private val downloadButton
    get() = binding.downloadOverdueListButton

  private val shareButton
    get() = binding.shareOverdueListButton

  private val overdueSearchChipInputTextView
    get() = binding.overdueSearchChipInputTextView

  private val hotEvents = PublishSubject.create<UiEvent>()

  private val disposable = CompositeDisposable()

  private val overdueSearchListAdapter = PagingItemAdapter(
      diffCallback = OverdueAppointmentSearchListItem.DiffCallback(),
      bindings = mapOf(
          R.layout.list_item_overdue_patient to { layoutInflater, parent ->
            ListItemOverduePatientBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_item_search_overdue_select_all_button to { layoutInflater, parent ->
            ListItemSearchOverdueSelectAllButtonBinding.inflate(layoutInflater, parent, false)
          }
      ),
      placeHolderBinding = R.layout.list_item_overdue_placeholder to { layoutInflater, parent ->
        ListItemOverduePlaceholderBinding.inflate(layoutInflater, parent, false)
      }
  )

  private val searchSuggestionsAdapter by unsafeLazy {
    ArrayAdapter<String>(
        requireContext(),
        R.layout.view_overdue_search_suggestion,
        R.id.suggestion_text_view,
        mutableListOf()
    )
  }

  override fun defaultModel() = OverdueSearchModel.create()

  override fun createUpdate(): OverdueSearchUpdate {
    val canGeneratePdf = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    return OverdueSearchUpdate(LocalDate.now(userClock), canGeneratePdf)
  }

  override fun createInit() = OverdueSearchInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<OverdueSearchViewEffect>) = effectHandlerFactory
      .create(
          viewEffectsConsumer = viewEffectsConsumer,
          pagingCacheScope = { lifecycleScope }
      )
      .build()

  override fun uiRenderer() = OverdueSearchUiRenderer(
      ui = this,
      isOverdueSelectAndDownloadEnabled = country.isoCountryCode == Country.INDIA
  )

  override fun viewEffectHandler() = OverdueSearchViewEffectHandler(this)

  override fun events() = Observable
      .mergeArray(
          overdueSearchListAdapter.itemEvents,
          hotEvents,
          clearSelectedOverdueAppointmentClicks(),
          downloadButtonClicks(),
          shareButtonClicks(),
          overdueSearchInputsChanges()
      )
      .compose(RequestPermissions(runtimePermissions, screenResults.streamResults().ofType()))
      .compose(runtimeNetworkStatus::apply)
      .compose(ReportAnalyticsEvents())
      .cast<OverdueSearchEvent>()

  private fun clearSelectedOverdueAppointmentClicks() = clearSelectedOverdueAppointmentsButton
      .clicks()
      .map { ClearSelectedOverdueAppointmentsClicked }

  private fun downloadButtonClicks(): Observable<UiEvent> = downloadButton
      .clicks()
      .map { DownloadButtonClicked() }

  private fun shareButtonClicks(): Observable<UiEvent> = shareButton
      .clicks()
      .map { ShareButtonClicked() }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onResume() {
    super.onResume()
    lightStatusBar(enabled = true)
  }

  override fun onStop() {
    lightStatusBar(enabled = false)
    super.onStop()
  }

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?): ScreenOverdueSearchBinding {
    return ScreenOverdueSearchBinding.inflate(layoutInflater, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    appbar.applyStatusBarPadding()
    downloadAndShareButtonFrame.applyInsetsBottomPadding()

    overdueSearchToolbar.setNavigationOnClickListener {
      router.pop()
    }

    overdueSearchRecyclerView.adapter = overdueSearchListAdapter

    overdueSearchChipInputTextView.showKeyboard()
    overdueSearchChipInputTextView.setDropdownAnchor(R.id.overdue_search_app_bar)

    disposable.addAll(
        hideKeyboardOnSearchResultsScroll(),
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

  override fun showSearchResults() {
    overdueSearchRecyclerView.visibility = VISIBLE
  }

  override fun hideSearchResults() {
    overdueSearchRecyclerView.visibility = GONE
  }

  override fun setOverdueSearchResultsPagingData(
      overdueSearchResults: PagingData<OverdueAppointment>,
      selectedOverdueAppointments: Set<UUID>
  ) {
    overdueSearchListAdapter.submitData(
        lifecycle,
        OverdueAppointmentSearchListItem.from(
            appointments = overdueSearchResults,
            selectedOverdueAppointments = selectedOverdueAppointments,
            clock = userClock,
            isOverdueSelectAndDownloadEnabled = country.isoCountryCode == Country.INDIA
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

  fun hideKeyboard() {
    binding.root.hideKeyboard()
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

  override fun openSelectDownloadFormatDialog() {
    router.push(SelectOverdueDownloadFormatDialog.Key(Download))
  }

  override fun openSelectShareFormatDialog() {
    router.push(SelectOverdueDownloadFormatDialog.Key(Share))
  }

  override fun openShareInProgressDialog() {
    router.push(SelectOverdueDownloadFormatDialog.Key(SharingInProgress))
  }

  override fun showNoInternetConnectionDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.overdue_download_no_active_network_connection_dialog_title)
        .setMessage(R.string.overdue_download_no_active_network_connection_dialog_message)
        .setPositiveButton(R.string.overdue_download_no_active_network_connection_dialog_positive_button, null)
        .show()
  }

  override fun setOverdueSearchSuggestions(searchSuggestions: List<String>) {
    searchSuggestionsAdapter.clear()
    searchSuggestionsAdapter.addAll(searchSuggestions)

    overdueSearchChipInputTextView.setAdapter(searchSuggestionsAdapter)
  }

  private fun overdueSearchInputsChanges(): Observable<UiEvent> {
    return overdueSearchChipInputTextView
        .inputChanges
        .debounce(500, TimeUnit.MILLISECONDS)
        .map(::OverdueSearchInputsChanged)
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
