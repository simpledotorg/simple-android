package org.simple.clinic.drugstockreminders.enterdrugstock

import com.f2prateek.rx.preferences2.Preference
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.Optional

class EnterDrugStockEffectHandlerTest {

  private val drugStockFormUrlPreference = mock<Preference<Optional<String>>>()
  private val effectHandler = EnterDrugStockEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      drugStockFormUrlPreference = drugStockFormUrlPreference
  )
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load enter drug stock form url effect is received, then load the url`() {
    // given
    val drugStockFormUrl = Optional.of("drug_stock_form_url")
    whenever(drugStockFormUrlPreference.get()) doReturn drugStockFormUrl

    // when
    effectHandlerTestCase.dispatch(LoadDrugStockFormUrl)

    // then
    effectHandlerTestCase.assertOutgoingEvents(DrugStockFormUrlLoaded(drugStockFormUrl))
  }
}
