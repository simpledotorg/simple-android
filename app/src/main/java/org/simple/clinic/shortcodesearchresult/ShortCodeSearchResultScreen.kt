package org.simple.clinic.shortcodesearchresult

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.patient_search_view.view.*
import kotlinx.android.synthetic.main.screen_shortcode_search_result.view.*
import org.simple.clinic.R
import org.simple.clinic.ViewControllerBinding
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.di.injector
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchScreenKey
import org.simple.clinic.searchresultsview.PatientSearchResults
import org.simple.clinic.searchresultsview.SearchResultsItemType
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.text.style.TextAppearanceWithLetterSpacingSpan
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class ShortCodeSearchResultScreen(context: Context, attributes: AttributeSet) : RelativeLayout(context, attributes), ShortCodeSearchResultUi {

  @Inject
  lateinit var uiChangeProducer: ShortCodeSearchResultUiChangeProducer

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var bloodPressureDao: BloodPressureMeasurement.RoomDao

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  private lateinit var binding: ViewControllerBinding<UiEvent, ShortCodeSearchResultState, ShortCodeSearchResultUi>

  private val adapter = ItemAdapter(SearchResultsItemType.DiffCallback())

  private lateinit var disposable: Disposable

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    hideKeyboard()

    context.injector<Injector>().inject(this)

    val screenKey = screenRouter.key<ShortCodeSearchResultScreenKey>(this)
    setupToolBar(screenKey)
    setupScreen()
    setupViewControllerBinding(screenKey)
    setupClickEvents()
  }

  override fun onDetachedFromWindow() {
    if (::disposable.isInitialized) disposable.dispose()
    super.onDetachedFromWindow()
  }

  private fun setupToolBar(screenKey: ShortCodeSearchResultScreenKey) {
    toolBar.title = formatShortCodeForDisplay(context, screenKey.shortCode)

    with(toolBar) {
      setNavigationOnClickListener { screenRouter.pop() }
      setOnClickListener { screenRouter.pop() }
    }
  }

  private fun setupViewControllerBinding(screenKey: ShortCodeSearchResultScreenKey) {
    val uiStateProducer = ShortCodeSearchResultStateProducer(
        shortCode = screenKey.shortCode,
        patientRepository = patientRepository,
        userSession = userSession,
        facilityRepository = facilityRepository,
        bloodPressureDao = bloodPressureDao,
        ui = this,
        schedulersProvider = schedulersProvider
    )
    binding = ViewControllerBinding.bindToView(this, uiStateProducer, uiChangeProducer)
  }

  private fun setupClickEvents() {
    newPatientButton.setOnClickListener { binding.onEvent(SearchPatient) }
    disposable = adapter
        .itemEvents
        .ofType<SearchResultsItemType.Event.ResultClicked>()
        .map { binding.onEvent(ViewPatient(it.patientUuid)) }
        .subscribe()
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
