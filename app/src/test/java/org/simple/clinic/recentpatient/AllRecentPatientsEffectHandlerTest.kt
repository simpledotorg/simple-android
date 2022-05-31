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
import org.simple.sharedTestCode.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.PagingSourceFactory
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.LocalDate
import java.util.UUID

class AllRecentPatientsEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val currentFacility = { TestData.facility(uuid = UUID.fromString("43adbee6-a7cf-4482-b673-8f1111bb09a4")) }
  private val uiActions = mock<AllRecentPatientsUiActions>()
  private val pagerFactory = mock<PagerFactory>()
  private val pagingSize = 25
  private val effectHandler = AllRecentPatientsEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      currentFacility = currentFacility,
      pagerFactory = pagerFactory,
      allRecentPatientsPagingSize = pagingSize,
      viewEffectsConsumer = AllRecentPatientViewEffectHandler(uiActions)::handle
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
        TestData.recentPatient(
            uuid = UUID.fromString("605882e4-f617-4e3d-8dbc-512ed2915996"),
            patientAgeDetails = PatientAgeDetails(
                ageValue = null,
                ageUpdatedAt = null,
                dateOfBirth = LocalDate.parse("1976-01-01")
            )
        ),
        TestData.recentPatient(
            uuid = UUID.fromString("d76962b4-9c5f-4c8b-8258-d9dbf73366d6"),
            patientAgeDetails = PatientAgeDetails(
                ageValue = null,
                ageUpdatedAt = null,
                dateOfBirth = LocalDate.parse("1976-01-01")
            )
        )
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
        TestData.recentPatient(
            uuid = UUID.fromString("85b26146-2809-4ae8-b544-a0967b645d2b"),
            patientAgeDetails = PatientAgeDetails(
                ageValue = null,
                ageUpdatedAt = null,
                dateOfBirth = LocalDate.parse("1976-01-01")
            )
        ),
        TestData.recentPatient(
            uuid = UUID.fromString("251bf646-f739-4d68-a710-3ace885096ac"),
            patientAgeDetails = PatientAgeDetails(
                ageValue = null,
                ageUpdatedAt = null,
                dateOfBirth = LocalDate.parse("1976-01-01")
            )
        ),
    ))

    whenever(pagerFactory.createPager(
        sourceFactory = any<PagingSourceFactory<Int, RecentPatient>>(),
        pageSize = eq(pagingSize),
        enablePlaceholders = eq(false),
        initialKey = eq(null)
    )) doReturn Observable.just(recentPatients)

    // when
    effectHandlerTestCase.dispatch(LoadAllRecentPatients)

    // then
    verifyZeroInteractions(uiActions)

    effectHandlerTestCase.assertOutgoingEvents(RecentPatientsLoaded(recentPatients))
  }

  @Test
  fun `when open patient summary effect is received, then open patient summary screen`() {
    // given
    val patientUuid = UUID.fromString("6891045b-7c6a-4b4f-89b8-9bc25f114295")

    // when
    effectHandlerTestCase.dispatch(OpenPatientSummary(patientUuid))

    // then
    verify(uiActions).openPatientSummary(patientUuid)
    verifyNoMoreInteractions(uiActions)

    effectHandlerTestCase.assertNoOutgoingEvents()
  }
}
