package org.simple.clinic.monthlyreport.list

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.util.UUID

class MonthlyReportListEffectHandlerTest {

  private val ui = mock<MonthlyReportListUi>()
  private val questionnaireResponseRepository = mock<QuestionnaireResponseRepository>()
  private val viewEffectHandler = MonthlyReportListViewEffectHandler(ui)
  private val questionnaireResponse = TestData.questionnaireResponse(uuid = UUID.fromString("674517cf-8dfa-466f-b65e-76fffb40c230"))

  private val facility = TestData.facility(
      uuid = UUID.fromString("e8075335-f766-4605-8216-41bf79189609"),
      name = "PHC Simple"
  )

  private val effectHandler = MonthlyReportListEffectHandler(
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
  fun `when load questionnaire response list effect is received then questionnaire response list should be fetched`() {
    whenever(questionnaireResponseRepository.questionnaireResponsesFilteredBy(MonthlyScreeningReports, facility.uuid)) doReturn Observable.just(listOf(questionnaireResponse))

    //when
    testCase.dispatch(LoadMonthlyReportListEffect(MonthlyScreeningReports))

    //then
    testCase.assertOutgoingEvents(MonthlyReportListFetched(listOf(questionnaireResponse)))
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when load current facility effect is received, then load current facility`() {
    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyZeroInteractions(ui)
  }
}
