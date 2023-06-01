package org.simple.clinic.monthlyreports.complete

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.util.UUID

class MonthlyReportCompleteEffectHandlerTest {

  private val ui = mock<MonthlyReportCompleteUi>()
  private val questionnaireResponseRepository = mock<QuestionnaireResponseRepository>()
  private val viewEffectHandler = MonthlyReportCompleteViewEffectHandler(ui)

  private val effectHandler = MonthlyReportCompleteEffectHandler(
      questionnaireResponseRepository = questionnaireResponseRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      viewEffectsConsumer = viewEffectHandler::handle
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load questionnaire response effect is received then questionnaire response should be fetched`() {
    //given
    val questionnaireResponse = TestData.questionnaireResponse(
        uuid = UUID.fromString("825423b6-4639-44e9-b11d-c5da5ede8071")
    )

    whenever(questionnaireResponseRepository.questionnaireResponse(questionnaireResponse.uuid)) doReturn questionnaireResponse

    //when
    testCase.dispatch(LoadQuestionnaireResponseEffect(questionnaireResponse.uuid))

    //then
    testCase.assertOutgoingEvents(QuestionnaireResponseFetched(questionnaireResponse))
    verifyNoInteractions(ui)
  }

  @Test
  fun `when go to monthly screening reports effect is received, then go to the same`() {
    // when
    testCase.dispatch(GoToMonthlyReportsScreen)

    // then
    testCase.assertNoOutgoingEvents()

    verify(ui).goToMonthlyReportsScreen()
    verifyNoMoreInteractions(ui)
  }
}
