package org.simple.clinic.recentpatient

import androidx.paging.PagingData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class AllRecentPatientsEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val currentFacility = { TestData.facility(uuid = UUID.fromString("43adbee6-a7cf-4482-b673-8f1111bb09a4")) }
  private val uiActions = mock<AllRecentPatientsUiActions>()
  private val effectHandler = AllRecentPatientsEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      currentFacility = currentFacility,
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(
      effectHandler = effectHandler
  )

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when show recent patients effect is received, then show recent patients`() {
    // given
    val recentPatients = PagingData.from(listOf(
        TestData.recentPatient(uuid = UUID.fromString("605882e4-f617-4e3d-8dbc-512ed2915996")),
        TestData.recentPatient(uuid = UUID.fromString("d76962b4-9c5f-4c8b-8258-d9dbf73366d6"))
    ))

    // when
    effectHandlerTestCase.dispatch(ShowRecentPatients(recentPatients))

    // then
    verify(uiActions).showRecentPatients(recentPatients)
    verifyNoMoreInteractions(uiActions)

    effectHandlerTestCase.assertNoOutgoingEvents()
  }
}
