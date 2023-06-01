package org.simple.clinic.monthlyreports.list

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.util.UUID

class MonthlyReportsEffectHandlerTest {

  private val ui = mock<MonthlyReportsUi>()
  private val questionnaireResponseRepository = mock<QuestionnaireResponseRepository>()
  private val viewEffectHandler = MonthlyReportsViewEffectHandler(ui)
  private val questionnaireResponse = TestData.questionnaireResponse(uuid = UUID.fromString("674517cf-8dfa-466f-b65e-76fffb40c230"))

  private val facility = TestData.facility(
      uuid = UUID.fromString("e8075335-f766-4605-8216-41bf79189609"),
      name = "PHC Simple"
  )

  private val effectHandler = MonthlyReportsEffectHandler(
      questionnaireResponseRepository = questionnaireResponseRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      viewEffectsConsumer = viewEffectHandler::handle,
      currentFacility = { facility },
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load monthly reports effect is received then questionnaire response list should be fetched`() {
    whenever(questionnaireResponseRepository.questionnaireResponsesFilteredBy(MonthlyScreeningReports, facility.uuid)) doReturn Observable.just(listOf(questionnaireResponse))

    //when
    testCase.dispatch(LoadMonthlyReportsEffect(MonthlyScreeningReports))

    //then
    testCase.assertOutgoingEvents(MonthlyReportsFetched(listOf(questionnaireResponse)))
    verifyNoInteractions(ui)
  }

  @Test
  fun `when load current facility effect is received, then load current facility`() {
    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyNoInteractions(ui)
  }
}
