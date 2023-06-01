package org.simple.clinic.drugstockreminders.enterdrugstock

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test

class EnterDrugStockScreenUiRendererTest {

  @Test
  fun `when screen is created, then load the drug stock form`() {
    // given
    val ui = mock<EnterDrugStockUi>()
    val defaultModel = EnterDrugStockModel.create()
    val uiRenderer = EnterDrugStockScreenUiRenderer(ui)
    val drugStockFormUrl = "drug_stock_form_url"

    // when
    uiRenderer.render(defaultModel.drugStockFormUrlLoaded(drugStockFormUrl))

    // then
    verify(ui).loadDrugStockForm(drugStockFormUrl)
    verifyNoMoreInteractions(ui)
  }
}
