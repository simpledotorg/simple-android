package org.simple.clinic.facility.change.confirm

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
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

  @Test
  fun `when facility change effect is received, then change user's current facility`() {
    //given
    val userSession = mock<UserSession>()
    val facilityRepository = mock<FacilityRepository>()
    val reportsRepository = mock<ReportsRepository>()
    val reportsSync = mock<ReportsSync>()
    val effectHandler = ConfirmFacilityChangeEffectHandler(facilityRepository, userSession, reportsRepository, reportsSync, TrampolineSchedulersProvider())
    val testCase = EffectHandlerTestCase(effectHandler.build())

    val facility = TestData.facility(UUID.fromString("98a260cb-45b1-46f7-a7ca-d217a27c43c0"))
    val loggedInUser = TestData.loggedInUser(UUID.fromString("7d8dce15-701b-4cf9-8dee-e003c51ccde9"))

    whenever(userSession.loggedInUserImmediate()) doReturn loggedInUser
    whenever(facilityRepository.associateUserWithFacility(loggedInUser, facility)) doReturn Completable.complete()
    whenever(facilityRepository.setCurrentFacility(loggedInUser, facility)) doReturn Completable.complete()
    whenever(reportsRepository.deleteReportsFile()).thenReturn(Single.just(DeleteFileResult.Success))
    whenever(reportsSync.sync()) doReturn Completable.complete()

    //when
    testCase.dispatch(ChangeFacilityEffect(facility) as ConfirmFacilityChangeEffect)

    //then
    testCase.assertOutgoingEvents(FacilityChanged)
    verify(reportsRepository).deleteReportsFile()
    verify(reportsSync).sync()
  }
}
