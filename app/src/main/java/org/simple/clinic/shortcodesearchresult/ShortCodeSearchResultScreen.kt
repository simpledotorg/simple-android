package org.simple.clinic.shortcodesearchresult

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListPatientSearchHeaderBinding
import org.simple.clinic.databinding.ListPatientSearchNoPatientsBinding
import org.simple.clinic.databinding.ListPatientSearchOldBinding
import org.simple.clinic.databinding.ScreenShortcodeSearchResultBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchScreenKey
import org.simple.clinic.searchresultsview.PatientSearchResults
import org.simple.clinic.searchresultsview.SearchResultsItemType
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.text.style.TextAppearanceWithLetterSpacingSpan
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.hideKeyboard
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class ShortCodeSearchResultScreen(
    context: Context,
    attributes: AttributeSet
) : RelativeLayout(context, attributes), ShortCodeSearchResultUi, UiActions {

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandlerFactory: ShortCodeSearchResultEffectHandler.Factory

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

  private val events by unsafeLazy {
    Observable
        .merge(
            searchPatientClicks(),
            patientItemClicks()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val screenKey by unsafeLazy { screenRouter.key<ShortCodeSearchResultScreenKey>(this) }

  private val delegate by unsafeLazy {
    val uiRenderer = UiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = ShortCodeSearchResultState.fetchingPatients(screenKey.shortCode),
        update = ShortCodeSearchResultUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render,
        init = ShortCodeSearchResultInit()
    )
  }

  private var viewBinding: ScreenShortcodeSearchResultBinding? = null

  private val toolBar
    get() = viewBinding!!.toolBar

  private val patientSearchView
    get() = viewBinding!!.patientSearchView

  private val newPatientButton
    get() = patientSearchView.newPatientButton

  private val resultsRecyclerView
    get() = patientSearchView.resultsRecyclerView

  private val loader
    get() = patientSearchView.loader

  private val newPatientContainer
    get() = patientSearchView.newPatientContainer

  private val emptyStateView
    get() = patientSearchView.emptyStateView

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    hideKeyboard()

    viewBinding = ScreenShortcodeSearchResultBinding.bind(this)

    context.injector<Injector>().inject(this)

    setupToolBar()
    setupScreen()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    viewBinding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun setupToolBar() {
    toolBar.title = formatShortCodeForDisplay(context, screenKey.shortCode)

    with(toolBar) {
      setNavigationOnClickListener { screenRouter.pop() }
      setOnClickListener { screenRouter.pop() }
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
    screenRouter.push(PatientSummaryScreenKey(
        patientUuid = patientUuid,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  override fun openPatientSearch() {
    screenRouter.push(PatientSearchScreenKey(additionalIdentifier = null))
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

  private fun formatShortCodeForDisplay(
      textSpan: TextAppearanceWithLetterSpacingSpan,
      shortCode: String
  ): CharSequence {
    // TODO (vs): 14/12/20 Make this change
    // This is duplicated in `Identifier.displayValue()`, but unifying
    // it requires us to change the screen key of this screen to accept
    // Identifier, which will have cascading changes throughout the
    // screen. We will look into changing this once we migrate this
    // screen to Mobius.
    val prefix = shortCode.substring(0, 3)
    val suffix = shortCode.substring(3)
    val formattedShortCode = "$prefix${Unicode.nonBreakingSpace}$suffix"

    return Truss()
        .pushSpan(textSpan)
        .append(formattedShortCode)
        .popSpan()
        .build()

  }

  private fun formatShortCodeForDisplay(context: Context, shortCode: String): CharSequence {
    val textSpacingSpan = TextAppearanceWithLetterSpacingSpan(context, R.style.Clinic_V2_TextAppearance_Body0Left_Numeric_White100)
    return formatShortCodeForDisplay(textSpacingSpan, shortCode)
  }

  interface Injector {
    fun inject(target: ShortCodeSearchResultScreen)
  }
}
