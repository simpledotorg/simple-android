package org.simple.clinic.facility.change.confirm

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.reports.ReportsSync
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Instant
import java.util.UUID

class ConfirmFacilityChangeEffectHandlerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val facilityRepository = mock<FacilityRepository>()
  private val reportsRepository = mock<ReportsRepository>()
  private val reportsSync = mock<ReportsSync>()
  private val uiActions = mock<ConfirmFacilityChangeUiActions>()
  private val isFacilitySwitchedPreference = mock<Preference<Boolean>>()
  private val facilitySyncGroupSwitchedAtPreference = mock<Preference<Optional<Instant>>>()

  private val effectHandler = ConfirmFacilityChangeEffectHandler(
      facilityRepository = facilityRepository,
      reportsRepository = reportsRepository,
      reportsSync = reportsSync,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions,
      isFacilitySwitchedPreference = isFacilitySwitchedPreference,
      facilitySyncGroupSwitchAtPreference = facilitySyncGroupSwitchedAtPreference
  )
  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @Test
  fun `when facility change effect is received, then change user's current facility`() {
    //given
    val facility = TestData.facility(UUID.fromString("98a260cb-45b1-46f7-a7ca-d217a27c43c0"))

    whenever(reportsRepository.deleteReports()) doReturn Completable.complete()
    whenever(reportsSync.sync()) doReturn Completable.complete()

    //when
    testCase.dispatch(ChangeFacilityEffect(facility))

    //then
    testCase.assertOutgoingEvents(FacilityChanged(facility))
    verify(reportsRepository).deleteReports()
    verify(reportsSync).sync()
    verify(isFacilitySwitchedPreference).set(true)
    verify(facilityRepository).setCurrentFacilityImmediate(facility)
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

  @Test
  fun `when the load current facility effect is received, load the current facility`() {
    // given
    val facility = TestData.facility(UUID.fromString("98a260cb-45b1-46f7-a7ca-d217a27c43c0"))
    whenever(facilityRepository.currentFacilityImmediate()) doReturn facility

    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyZeroInteractions(uiActions)
  }
}
