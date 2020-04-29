package org.simple.clinic.facility.change.confirm

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.reports.ReportsSync
import org.simple.clinic.storage.files.DeleteFileResult
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class ConfirmFacilityChangeEffectHandlerTest {

  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val reportsRepository = mock<ReportsRepository>()
  private val reportsSync = mock<ReportsSync>()
  private val uiActions = mock<ConfirmFacilityChangeUiActions>()
  private val isFacilitySwitchedPreference = mock<Preference<Boolean>>()

  private val effectHandler = ConfirmFacilityChangeEffectHandler(facilityRepository, userSession, reportsRepository, reportsSync, TrampolineSchedulersProvider(), uiActions, isFacilitySwitchedPreference)
  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @Test
  fun `when facility change effect is received, then change user's current facility`() {
    //given
    val facility = TestData.facility(UUID.fromString("98a260cb-45b1-46f7-a7ca-d217a27c43c0"))
    val loggedInUser = TestData.loggedInUser(UUID.fromString("7d8dce15-701b-4cf9-8dee-e003c51ccde9"))

    whenever(userSession.loggedInUserImmediate()) doReturn loggedInUser
    whenever(facilityRepository.setCurrentFacility(loggedInUser, facility)) doReturn Completable.complete()
    whenever(reportsRepository.deleteReportsFile()).thenReturn(Single.just(DeleteFileResult.Success))
    whenever(reportsSync.sync()) doReturn Completable.complete()

    //when
    testCase.dispatch(ChangeFacilityEffect(facility))

    //then
    testCase.assertOutgoingEvents(FacilityChanged)
    verify(reportsRepository).deleteReportsFile()
    verify(reportsSync).sync()
    verify(isFacilitySwitchedPreference).set(true)
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when close sheet effect is received then the sheet should be closed`() {
    //when
    testCase.dispatch(CloseSheet)

    //then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).closeSheet()
    verifyNoMoreInteractions(uiActions)
  }
}
