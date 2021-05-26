package org.simple.clinic.shortcodesearchresult

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListPatientSearchHeaderBinding
import org.simple.clinic.databinding.ListPatientSearchNoPatientsBinding
import org.simple.clinic.databinding.ListPatientSearchOldBinding
import org.simple.clinic.databinding.PatientSearchViewBinding
import org.simple.clinic.databinding.ScreenShortcodeSearchResultBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Features
import org.simple.clinic.instantsearch.InstantSearchScreenKey
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.searchresultsview.PatientSearchResults
import org.simple.clinic.searchresultsview.SearchResultsItemType
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.hideKeyboard
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class ShortCodeSearchResultScreen :
    BaseScreen<
        ShortCodeSearchResultScreenKey,
        ScreenShortcodeSearchResultBinding,
        ShortCodeSearchResultState,
        ShortCodeSearchResultEvent,
        ShortCodeSearchResultEffect>(),
    ShortCodeSearchResultUi,
    UiActions {

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: ShortCodeSearchResultEffectHandler.Factory

  @Inject
  lateinit var features: Features

  private val adapter = ItemAdapter(
      diffCallback = SearchResultsItemType.DiffCallback(),
      bindings = mapOf(
          R.layout.list_patient_search_header to { layoutInflater, parent ->
            ListPatientSearchHeaderBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_patient_search_no_patients to { layoutInflater, parent ->
            ListPatientSearchNoPatientsBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_patient_search_old to { layoutInflater, parent ->
            ListPatientSearchOldBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private var patientSearchViewBinding: PatientSearchViewBinding? = null

  private val toolBar
    get() = binding.toolBar

  private val newPatientButton
    get() = patientSearchViewBinding!!.newPatientButton

  private val resultsRecyclerView
    get() = patientSearchViewBinding!!.resultsRecyclerView

  private val loader
    get() = patientSearchViewBinding!!.loader

  private val newPatientContainer
    get() = patientSearchViewBinding!!.newPatientContainer

  private val emptyStateView
    get() = patientSearchViewBinding!!.emptyStateView

  override fun defaultModel() = ShortCodeSearchResultState.fetchingPatients(screenKey.shortCode)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenShortcodeSearchResultBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = UiRenderer(this)

  override fun events() = Observable
      .merge(
          searchPatientClicks(),
          patientItemClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<ShortCodeSearchResultEvent>()

  override fun createUpdate() = ShortCodeSearchResultUpdate()

  override fun createInit() = ShortCodeSearchResultInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return super.onCreateView(inflater, container, savedInstanceState).also {
      patientSearchViewBinding = PatientSearchViewBinding.bind(binding.root)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    patientSearchViewBinding = null
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    (view as ViewGroup).hideKeyboard()
    setupToolBar()
    setupScreen()
  }

  private fun setupToolBar() {
    toolBar.title = formatShortCodeForDisplay(screenKey.shortCode)

    with(toolBar) {
      setNavigationOnClickListener { router.pop() }
      setOnClickListener { router.pop() }
    }
  }

  private fun searchPatientClicks(): Observable<ShortCodeSearchResultEvent> {
    return newPatientButton
        .clicks()
        .map { SearchPatient }
  }

  private fun patientItemClicks(): Observable<ShortCodeSearchResultEvent> {
    return adapter
        .itemEvents
        .ofType<SearchResultsItemType.Event.ResultClicked>()
        .map { ViewPatient(it.patientUuid) }
  }

  private fun setupScreen() {
    newPatientButton.text = resources.getString(R.string.shortcodesearchresult_enter_patient_name_button)

    resultsRecyclerView.layoutManager = LinearLayoutManager(context)
    resultsRecyclerView.adapter = adapter
  }

  override fun openPatientSummary(patientUuid: UUID) {
    router.push(PatientSummaryScreenKey(
        patientUuid = patientUuid,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  override fun openPatientSearch() {
    val screenKey = InstantSearchScreenKey(additionalIdentifier = null, initialSearchQuery = null)

    router.push(screenKey)
  }

  override fun showLoading() {
    loader.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    loader.visibility = View.GONE
  }

  override fun showSearchResults(foundPatients: PatientSearchResults) {
    adapter.submitList(SearchResultsItemType.from(foundPatients))
  }

  override fun showSearchPatientButton() {
    newPatientContainer.visibility = View.VISIBLE
  }

  override fun showNoPatientsMatched() {
    emptyStateView.visibility = View.VISIBLE
  }

  private fun formatShortCodeForDisplay(shortCode: String): CharSequence {
    // TODO (vs): 14/12/20 Make this change
    // This is duplicated in `Identifier.displayValue()`, but unifying
    // it requires us to change the screen key of this screen to accept
    // Identifier, which will have cascading changes throughout the
    // screen. We will look into changing this once we migrate this
    // screen to Mobius.
    val prefix = shortCode.substring(0, 3)
    val suffix = shortCode.substring(3)

    return "$prefix${Unicode.nonBreakingSpace}$suffix"
  }

  interface Injector {
    fun inject(target: ShortCodeSearchResultScreen)
  }
}
