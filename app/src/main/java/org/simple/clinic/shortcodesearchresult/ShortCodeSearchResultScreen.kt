package org.simple.clinic.shortcodesearchresult

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.patient_search_view.view.*
import kotlinx.android.synthetic.main.screen_shortcode_search_result.view.*
import org.simple.clinic.R
import org.simple.clinic.ViewControllerBinding
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchScreenKey
import org.simple.clinic.searchresultsview.PatientSearchResults
import org.simple.clinic.searchresultsview.SearchResultClicked
import org.simple.clinic.searchresultsview.SearchResultsItemType
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.identifierdisplay.formatShortCodeForDisplay
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.threeten.bp.Instant
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

  private val adapter = GroupAdapter<ViewHolder>()

  private val downstreamEvents = PublishSubject.create<UiEvent>()

  private lateinit var disposable: Disposable

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    hideKeyboard()

    TheActivity.component.inject(this)

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
    disposable = downstreamEvents
        .ofType<SearchResultClicked>()
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
    screenRouter.push(PatientSearchScreenKey())
  }

  override fun showLoading() {
    loader.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    loader.visibility = View.GONE
  }

  override fun showSearchResults(foundPatients: PatientSearchResults) {
    val items = SearchResultsItemType
        .from(foundPatients)
        .onEach { it.uiEvents = downstreamEvents }
    adapter.update(items)
  }

  override fun showSearchPatientButton() {
    newPatientContainer.visibility = View.VISIBLE
  }

  override fun showNoPatientsMatched() {
    emptyStateView.visibility = View.VISIBLE
  }
}
