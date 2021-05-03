package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.plumbing.AsyncOp

class UiRenderer(
    private val ui: ShortCodeSearchResultUi
) : ViewRenderer<IdentifierSearchResultState> {

  override fun render(model: IdentifierSearchResultState) {
    when (model.fetchPatientsAsyncOp) {
      AsyncOp.IN_FLIGHT -> ui.showLoading()
      AsyncOp.SUCCEEDED -> showSearchResults(model)
      AsyncOp.IDLE, AsyncOp.FAILED -> { /* Nothing to do here */
      }
    }
  }

  private fun showSearchResults(model: IdentifierSearchResultState) {
    if (model.patients.hasNoResults)
      showNoPatientsFound()
    else
      showPatientsFound(model)
  }

  private fun showNoPatientsFound() {
    with(ui) {
      hideLoading()
      showSearchPatientButton()
      showNoPatientsMatched()
    }
  }

  private fun showPatientsFound(model: IdentifierSearchResultState) {
    with(ui) {
      hideLoading()
      showSearchPatientButton()
      showSearchResults(model.patients)
    }
  }
}
