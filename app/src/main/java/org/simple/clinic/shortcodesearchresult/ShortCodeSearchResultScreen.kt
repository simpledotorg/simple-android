package org.simple.clinic.shortcodesearchresult

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.SearchView
import kotlinx.android.synthetic.main.patient_search_view.view.*
import kotlinx.android.synthetic.main.screen_shortcode_search_result.view.*
import org.simple.clinic.R
import org.simple.clinic.ViewControllerBinding
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.allpatientsinfacility.PatientSearchResultUiState
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.text.style.TextAppearanceWithLetterSpacingSpan
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.ItemAdapter
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
  lateinit var schedulersProvider: SchedulersProvider

  private lateinit var screenKey: ShortCodeSearchResultScreenKey

  private lateinit var binding: ViewControllerBinding<UiEvent, ShortCodeSearchResultState, ShortCodeSearchResultUi>

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)
    screenKey = screenRouter.key(this)

    hideKeyboard()
    setupToolBar()

    val uiStateProducer = ShortCodeSearchResultStateProducer(screenKey.shortCode, patientRepository, this, schedulersProvider)
    binding = ViewControllerBinding.bindToView(this, uiStateProducer, uiChangeProducer)

    newPatientButton.text = resources.getString(R.string.shortcodesearchresult_enter_patient_name_button)
    setupClickEvents()
  }

  private fun setupClickEvents() {
    newPatientButton.setOnClickListener { binding.onEvent(SearchPatient) }
  }

  private fun setupToolBar() {
    val shortCode = screenKey.shortCode

    // This is guaranteed to be exactly 7 characters in length.
    val prefix = shortCode.substring(0, 3)
    val suffix = shortCode.substring(3)

    val formattedShortCode = "$prefix${Unicode.nonBreakingSpace}$suffix"

    val textSpacingSpan = TextAppearanceWithLetterSpacingSpan(context, R.style.Clinic_V2_TextAppearance_Body0Left_NumericBold_White100)

    toolBar.title = Truss()
        .pushSpan(textSpacingSpan)
        .append(formattedShortCode)
        .popSpan()
        .build()
  }

  override fun openPatientSummary(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewExistingPatient, Instant.now(utcClock)))
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

  override fun showSearchResults(foundPatients: List<PatientSearchResultUiState>) {
    TODO("not implemented")
  }

  override fun showSearchPatientButton() {
    newPatientContainer.visibility = View.VISIBLE
  }

  override fun showNoPatientsMatched() {
    emptyStateView.visibility = View.VISIBLE
  }
}
