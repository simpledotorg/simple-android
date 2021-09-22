package org.simple.clinic.drugs.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.jakewharton.rxbinding3.widget.textChanges
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListItemDrugSearchBinding
import org.simple.clinic.databinding.ListItemDrugSearchCornerCapBinding
import org.simple.clinic.databinding.ListItemDrugSearchDividerBinding
import org.simple.clinic.databinding.ScreenDrugsSearchBinding
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.selection.custom.CustomDrugEntrySheet
import org.simple.clinic.drugs.selection.custom.OpenAs
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.util.debounce
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.showKeyboard
import org.simple.clinic.widgets.visibleOrGone
import java.time.Duration
import java.util.UUID
import javax.inject.Inject

class DrugsSearchScreen : BaseScreen<
    DrugsSearchScreen.Key,
    ScreenDrugsSearchBinding,
    DrugSearchModel,
    DrugSearchEvent,
    DrugSearchEffect,
    Unit>(), DrugSearchUi, UiActions {

  @Inject
  lateinit var effectHandlerFactory: DrugSearchEffectHandler.Factory

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var drugFrequencyToLabelMap: Map<DrugFrequency?, DrugFrequencyLabel>

  private val adapter = PagingItemAdapter(
      diffCallback = DrugSearchListItem.DiffCallback(),
      bindings = mapOf(
          R.layout.list_item_drug_search to { layoutInflater, parent ->
            ListItemDrugSearchBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_item_drug_search_divider to { layoutInflater, parent ->
            ListItemDrugSearchDividerBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_item_drug_search_corner_cap to { layoutInflater, parent ->
            ListItemDrugSearchCornerCapBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val drugSearchToolbar
    get() = binding.drugSearchToolbar

  private val progressIndicator
    get() = binding.drugSearchProgressIndicator

  private val drugSearchResultsList
    get() = binding.drugSearchResultsList

  private val searchQueryEditText
    get() = binding.searchQueryEditText

  override fun defaultModel() = DrugSearchModel.create()

  override fun createUpdate() = DrugSearchUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) = effectHandlerFactory.create(this).build()

  override fun uiRenderer() = DrugSearchUiRenderer(this)

  override fun events() = Observable
      .mergeArray(
          searchQueryChanges(),
          drugListItemClicks(),
          newCustomDrugItemClicks())
      .compose(ReportAnalyticsEvents())
      .cast<DrugSearchEvent>()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ) = ScreenDrugsSearchBinding.inflate(layoutInflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    drugSearchToolbar.setNavigationOnClickListener {
      router.pop()
    }

    drugSearchResultsList.adapter = adapter
    adapter.addLoadStateListener(::drugSearchLoadStateListener)

    searchQueryEditText.showKeyboard()
  }

  override fun hideSearchResults() {
    drugSearchResultsList.visibility = View.GONE
    adapter.submitData(lifecycle, PagingData.empty())
  }

  override fun showSearchResults() {
    drugSearchResultsList.visibility = View.VISIBLE
  }

  override fun setDrugSearchResults(searchResults: PagingData<Drug>) {
    val searchQuery = searchQueryEditText.text?.toString().orEmpty()

    drugSearchResultsList.scrollToPosition(0)
    adapter.submitData(lifecycle, DrugSearchListItem.from(
        searchResults,
        searchQuery,
        drugFrequencyToLabelMap
    ))
  }

  override fun openCustomDrugEntrySheetFromDrugList(drugUuid: UUID, patientUuid: UUID) {
    router.push(CustomDrugEntrySheet.Key(OpenAs.New.FromDrugList(drugUuid), patientUuid))
  }

  override fun openCustomDrugEntrySheetFromDrugName(drugName: String, patientUuid: UUID) {
    router.push(CustomDrugEntrySheet.Key(OpenAs.New.FromDrugName(drugName), patientUuid))
  }

  private fun drugSearchLoadStateListener(combinedLoadStates: CombinedLoadStates) {
    val isLoading = combinedLoadStates.refresh is LoadState.Loading
    progressIndicator.visibleOrGone(isLoading)
  }

  private fun searchQueryChanges(): Observable<UiEvent> {
    return searchQueryEditText
        .textChanges()
        .skipInitialValue()
        .debounce(Duration.ofMillis(500))
        .map(CharSequence::trim)
        .map { searchQuery ->
          SearchQueryChanged(searchQuery.toString())
        }
  }

  private fun drugListItemClicks(): Observable<UiEvent> {
    return adapter
        .itemEvents
        .ofType<DrugSearchListItem.Event.DrugClicked>()
        .map { DrugListItemClicked(it.drug.id, screenKey.patientId) }
  }

  private fun newCustomDrugItemClicks(): Observable<UiEvent> {
    return adapter
        .itemEvents
        .ofType<DrugSearchListItem.Event.NewCustomDrugClicked>()
        .map { NewCustomDrugClicked(it.name, screenKey.patientId) }
  }

  interface Injector {
    fun inject(target: DrugsSearchScreen)
  }

  @Parcelize
  data class Key(
      val patientId: UUID,
      override val analyticsName: String = "Drug Search"
  ) : ScreenKey() {

    override fun instantiateFragment() = DrugsSearchScreen()
  }
}
