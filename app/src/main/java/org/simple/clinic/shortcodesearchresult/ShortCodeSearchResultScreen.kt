package org.simple.clinic.shortcodesearchresult

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.screen_shortcode_search_result.view.*
import org.simple.clinic.R
import org.simple.clinic.ViewControllerBinding
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.text.style.TextAppearanceWithLetterSpacingSpan
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class ShortCodeSearchResultScreen(context: Context, attributes: AttributeSet) : RelativeLayout(context, attributes), ShortCodeSearchResultUi {

  @Inject
  lateinit var uiStateProducer: ShortCodeSearchResultStateProducer

  @Inject
  lateinit var uiChangeProducer: ShortCodeSearchResultUiChangeProducer

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var screenRouter: ScreenRouter

  private lateinit var binding: ViewControllerBinding<UiEvent, ShortCodeSearchResultState, ShortCodeSearchResultUi>

  override fun onFinishInflate() {
    super.onFinishInflate()
    setupToolBar()

    binding = ViewControllerBinding.bindToView(this, uiStateProducer, uiChangeProducer)
  }

  private fun setupToolBar() {
    val screenKey = screenRouter.key<ShortCodeSearchResultScreenKey>(this)
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
    TODO("not implemented")
  }

  override fun openPatientSearch() {
    TODO("not implemented")
  }

  override fun showLoading() {
    TODO("not implemented")
  }

  override fun hideLoading() {
    TODO("not implemented")
  }

  override fun showSearchResults(foundPatients: List<PatientSearchResult>) {
    TODO("not implemented")
  }

  override fun showSearchPatientButton() {
    TODO("not implemented")
  }

  override fun showNoPatientsMatched() {
    TODO("not implemented")
  }
}
