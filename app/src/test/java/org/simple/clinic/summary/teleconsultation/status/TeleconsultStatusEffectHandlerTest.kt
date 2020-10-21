package org.simple.clinic.summary.teleconsultation.status

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleconsultStatusEffectHandlerTest {

  private val teleconsultRecordRepository = mock<TeleconsultRecordRepository>()
  private val effectHandler = TeleconsultStatusEffectHandler(
      teleconsultRecordRepository = teleconsultRecordRepository,
      schedulersProvider = TestSchedulersProvider.trampoline()
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
}
