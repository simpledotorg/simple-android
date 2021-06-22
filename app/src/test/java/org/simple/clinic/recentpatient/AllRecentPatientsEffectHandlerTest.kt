package org.simple.clinic.recentpatient

import androidx.paging.PagingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.PagingSourceFactory
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class AllRecentPatientsEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val currentFacility = { TestData.facility(uuid = UUID.fromString("43adbee6-a7cf-4482-b673-8f1111bb09a4")) }
  private val uiActions = mock<AllRecentPatientsUiActions>()
  private val pagerFactory = mock<PagerFactory>()
  private val effectHandler = AllRecentPatientsEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      currentFacility = currentFacility,
      pagerFactory = pagerFactory,
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

  @Test
  fun `when load recent patients effect is received, then load recent patients`() {
    // given
    val recentPatients = PagingData.from(listOf(
        TestData.recentPatient(uuid = UUID.fromString("85b26146-2809-4ae8-b544-a0967b645d2b")),
        TestData.recentPatient(uuid = UUID.fromString("251bf646-f739-4d68-a710-3ace885096ac"))
    ))

    whenever(pagerFactory.createPager(
        sourceFactory = any<PagingSourceFactory<Int, RecentPatient>>(),
        pageSize = eq(20),
        initialKey = eq(null)
    )) doReturn Observable.just(recentPatients)

    // when
    effectHandlerTestCase.dispatch(LoadAllRecentPatients)

    // then
    verifyZeroInteractions(uiActions)

    effectHandlerTestCase.assertOutgoingEvents(RecentPatientsLoaded(recentPatients))
  }
}
