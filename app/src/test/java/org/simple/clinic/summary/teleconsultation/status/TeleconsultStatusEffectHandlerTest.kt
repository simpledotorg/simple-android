package org.simple.clinic.summary.teleconsultation.status

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleconsultStatusEffectHandlerTest {

  private val uiActions = mock<TeleconsultStatusUiAction>()
  private val teleconsultRecordRepository = mock<TeleconsultRecordRepository>()
  private val effectHandler = TeleconsultStatusEffectHandler(
      teleconsultRecordRepository = teleconsultRecordRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  )
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when update teleconsult status effect is received, then update the teleconsultStatus`() {
    // given
    val teleconsultRecordId = UUID.fromString("5fa16206-b699-49c5-88ca-a0df27a70340")

    // when
    effectHandlerTestCase.dispatch(UpdateTeleconsultStatus(teleconsultRecordId, TeleconsultStatus.Yes))

    // then
    verify(teleconsultRecordRepository).updateRequesterCompletionStatus(teleconsultRecordId, TeleconsultStatus.Yes)
    verifyNoMoreInteractions(teleconsultRecordRepository)
  }

  @Test
  fun `when close sheet effect is received, then dismiss the sheet`() {
    // when
    effectHandlerTestCase.dispatch(CloseSheet)

    // then
    verify(uiActions).dismissSheet()
    verifyNoMoreInteractions(uiActions)
  }
}
