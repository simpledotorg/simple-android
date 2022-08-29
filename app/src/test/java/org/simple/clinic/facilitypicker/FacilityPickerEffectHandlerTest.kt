package org.simple.clinic.facilitypicker

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class FacilityPickerEffectHandlerTest {

  private val facilityRepository = mock<FacilityRepository>()
  private val appConfigRepository = mock<AppConfigRepository>()
  private val uiActions = mock<FacilityPickerUiActions>()

  private val effectHandler = FacilityPickerEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      screenLocationUpdates = mock(),
      facilityRepository = facilityRepository,
      appConfigRepository = appConfigRepository,
      uiActions = uiActions
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @Test
  fun `when the load facilities effect is received, the list of all facilities must be queried`() {
    // given
    val facilities = listOf(
        TestData.facility(uuid = UUID.fromString("7f0e9fc7-22b6-4b55-a750-8ff6f1a087e8"), name = "PHC Obvious"),
        TestData.facility(uuid = UUID.fromString("42a32bd5-eb86-438f-97cf-37870f8c6049"), name = "CHC Nilenso")
    )
    val searchQuery = "HC"

    whenever(appConfigRepository.currentState()) doReturn null
    whenever(facilityRepository.facilities(searchQuery)) doReturn Observable.just(facilities)

    // when
    testCase.dispatch(LoadFacilitiesWithQuery(searchQuery))

    // then
    testCase.assertOutgoingEvents(FacilitiesFetched(searchQuery, facilities))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the load facilities effect is received, the list of all facilities in current state must be queried`() {
    // given
    val phcObvious = TestData.facility(uuid = UUID.fromString("7f0e9fc7-22b6-4b55-a750-8ff6f1a087e8"), name = "PHC Obvious", state = "Andhra Pradesh")
    val chcNilenso = TestData.facility(uuid = UUID.fromString("42a32bd5-eb86-438f-97cf-37870f8c6049"), name = "CHC Nilenso", state = "Kerala")

    val facilities = listOf(phcObvious, chcNilenso)
    val searchQuery = "HC"

    whenever(appConfigRepository.currentState()) doReturn "Andhra Pradesh"
    whenever(facilityRepository.facilities(searchQuery)) doReturn Observable.just(facilities)

    // when
    testCase.dispatch(LoadFacilitiesWithQuery(searchQuery))

    // then
    testCase.assertOutgoingEvents(FacilitiesFetched(searchQuery, listOf(phcObvious)))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the load facilities in current group effect is received, the list of facilities in the current group must be queried`() {
    // given
    val facilities = listOf(
        TestData.facility(uuid = UUID.fromString("7f0e9fc7-22b6-4b55-a750-8ff6f1a087e8"), name = "PHC Obvious"),
        TestData.facility(uuid = UUID.fromString("42a32bd5-eb86-438f-97cf-37870f8c6049"), name = "CHC Nilenso")
    )
    val searchQuery = "HC"
    whenever(facilityRepository.facilitiesInCurrentGroup(searchQuery)) doReturn Observable.just(facilities)

    // when
    testCase.dispatch(LoadFacilitiesInCurrentGroup(searchQuery))

    // then
    testCase.assertOutgoingEvents(FacilitiesFetched(searchQuery, facilities))
    verifyZeroInteractions(uiActions)
  }
}
