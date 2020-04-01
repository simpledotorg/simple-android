package org.simple.clinic.summary.bloodsugar

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class BloodSugarSummaryViewEffectHandlerTest {

  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val uiActions = mock<UiActions>()
  private val config = mock<BloodSugarSummaryConfig>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val isFacilitySwitched = mock<Preference<Boolean>>()
  private val effectHandler = BloodSugarSummaryViewEffectHandler(
      bloodSugarRepository,
      TrampolineSchedulersProvider(),
      uiActions,
      config,
      userSession,
      facilityRepository,
      isFacilitySwitched
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)
  private val patientUuid = UUID.fromString("69cdea01-fbd8-437a-844c-25e412f32a9e")

  @Test
  fun `when fetch blood sugar effect is received then blood sugar should be fetched`() {
    //given
    val measurements = listOf<BloodSugarMeasurement>()
    whenever(bloodSugarRepository.latestMeasurements(patientUuid = patientUuid, limit = config.numberOfBloodSugarsToDisplay)).thenReturn(Observable.just(measurements))

    //when
    testCase.dispatch(FetchBloodSugarSummary(patientUuid))

    //then
    testCase.assertOutgoingEvents(BloodSugarSummaryFetched(measurements))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open blood sugar type selector effect is received then type selector sheet should be opened`() {
    //when
    testCase.dispatch(OpenBloodSugarTypeSelector)

    //then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showBloodSugarTypeSelector()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load blood sugars count effect is received, then load blood sugars count`() {
    // given
    val bloodSugarsCount = 10
    whenever(bloodSugarRepository.bloodSugarsCount(patientUuid)) doReturn Observable.just(bloodSugarsCount)

    // when
    testCase.dispatch(FetchBloodSugarCount(patientUuid))

    // then
    testCase.assertOutgoingEvents(BloodSugarCountFetched(bloodSugarsCount))
  }

  @Test
  fun `when show blood sugar history screen effect is received, then show blood sugar history screen`() {
    // when
    testCase.dispatch(ShowBloodSugarHistoryScreen(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showBloodSugarHistoryScreen(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open blood sugar update sheet effect is received, then open blood sugar update sheet`() {
    // given
    val bloodSugar = TestData.bloodSugarMeasurement(
        UUID.fromString("3be65af9-324f-4904-9ab4-6d8c47941b99"),
        patientUuid = patientUuid
    )

    // when
    testCase.dispatch(OpenBloodSugarUpdateSheet(bloodSugar))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodSugarUpdateSheet(bloodSugar.uuid, Random)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when should show alert facility change effect is received, then return the switch facility flag value`() {
    //given
    whenever(isFacilitySwitched.get()) doReturn true

    //when
    testCase.dispatch(ShouldShowAlertFacilityChange)

    //then
    testCase.assertOutgoingEvents(ShowAlertFacilityChangeEvent(true))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when show alert facility change effect is received then alert facility change sheet should be opened`() {
    //given
    val facility = TestData.facility(uuid = UUID.fromString("2687bf2c-7ce0-4631-8d21-202e994534df"))

    //when
    testCase.dispatch(OpenAlertFacilityChangeSheet(facility))

    //then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showAlertFacilityChangeSheet(facility.name)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when fetch current facility effect is received then current facility should be returned`() {
    //given
    val facility = TestData.facility(uuid = UUID.fromString("5e349788-c8b3-47e3-a485-961221ed12e3"))
    val user = TestData.loggedInUser(uuid = UUID.fromString("9b3bb80b-2c92-4a42-b591-573157b5e408"))
    whenever(userSession.loggedInUserImmediate()) doReturn user
    whenever(facilityRepository.currentFacility(user)) doReturn Observable.just(facility)

    //when
    testCase.dispatch(FetchCurrentFacility)

    //then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyZeroInteractions(uiActions)
  }

}
