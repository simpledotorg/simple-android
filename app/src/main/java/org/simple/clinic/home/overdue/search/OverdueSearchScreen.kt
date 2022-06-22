package org.simple.clinic.home.overdue.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.LoadState
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.databinding.ListItemOverduePatientBinding
import org.simple.clinic.databinding.ListItemOverduePlaceholderBinding
import org.simple.clinic.databinding.ScreenOverdueSearchBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class OverdueSearchScreen : BaseScreen<
    OverdueSearchScreen.Key,
    ScreenOverdueSearchBinding,
    OverdueSearchModel,
    OverdueSearchEvent,
    OverdueSearchEffect,
    Nothing>() {

  @Inject
  lateinit var router: Router

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

  private val disposable = CompositeDisposable()

  private val overdueSearchToolbar
    get() = binding.overdueSearchToolbar

  private val overdueSearchRecyclerView
    get() = binding.overdueSearchResults

  private val overdueSearchProgressIndicator
    get() = binding.overdueSearchProgressIndicator

  private val noOverdueSearchResultsContainer
    get() = binding.noOverdueSearchResultsContainer

  override fun defaultModel(): OverdueSearchModel {
    return OverdueSearchModel.create()
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

  override fun onDestroyView() {
    super.onDestroyView()
    disposable.clear()
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
