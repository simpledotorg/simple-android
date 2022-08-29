package org.simple.clinic.drugstockreminders.enterdrugstock

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
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
