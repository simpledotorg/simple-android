package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleconsultRecordEffectHandlerTest {

  private val teleconsultRecordRepository = mock<TeleconsultRecordRepository>()
  private val uiActions = mock<UiActions>()
  private val effectHandler = TeleconsultRecordEffectHandler(
      teleconsultRecordRepository = teleconsultRecordRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when go back effect is received, then go back to previous screen`() {
    // when
    effectHandlerTestCase.dispatch(GoBack)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).goBackToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when navigate to teleconsult success screen effect is received, then navigate to teleconsult success screen`() {
    // given
    val teleconsultRecordId = UUID.fromString("4d30c778-dec0-48f0-90a0-acf4d568eb6e")

    // when
    effectHandlerTestCase.dispatch(NavigateToTeleconsultSuccess(teleconsultRecordId))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).navigateToTeleconsultSuccessScreen(teleconsultRecordId)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load teleconsult record with prescribed drugs effect is received, then load the teleconsult record with prescribed drugs`() {
    // given
    val teleconsultRecordId = UUID.fromString("e7ba26e3-0362-42a6-aa2c-52abfba13677")
    val teleconsultRecordWithPrescribedDrugs = TestData.teleconsultRecordWithPrescribedDrugs(
        teleconsultRecord = TestData.teleconsultRecord(
            id = teleconsultRecordId
        ),
        prescribedDrugs = emptyList()
    )

    whenever(teleconsultRecordRepository.getTeleconsultRecordWithPrescribedDrugs(teleconsultRecordId)) doReturn teleconsultRecordWithPrescribedDrugs

    // when
    effectHandlerTestCase.dispatch(LoadTeleconsultRecordWithPrescribedDrugs(teleconsultRecordId))

    // then
    effectHandlerTestCase.assertOutgoingEvents(TeleconsultRecordWithPrescribedDrugsLoaded(teleconsultRecordWithPrescribedDrugs))

    verifyZeroInteractions(uiActions)
  }
}
