package org.simple.clinic.monthlyReports.questionnaire.entry

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.monthlyReports.questionnaire.MonthlyScreeningReports
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.util.UUID

class QuestionnaireEntryEffectHandlerTest {

  private val ui = mock<QuestionnaireEntryUi>()
  private val questionnaireRepository = mock<QuestionnaireRepository>()
  private val viewEffectHandler = QuestionnaireEntryViewEffectHandler(ui)
  private val questionnaireType = MonthlyScreeningReports
  private val questionnaire = TestData.questionnaire(uuid = UUID.fromString("85d0b5f1-af84-4a6b-938e-5166f8c27666"))

  private val facility = TestData.facility(
      uuid = UUID.fromString("e8075335-f766-4605-8216-41bf79189609"),
      name = "PHC Simple"
  )

  private val effectHandler = QuestionnaireEntryEffectHandler(
      questionnaireRepository = questionnaireRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      viewEffectsConsumer = viewEffectHandler::handle,
      currentFacility = Observable.just(facility),
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load questionnaire form effect is received then questionnaire form should be fetched`() {
    //given
    val questionnaire = TestData.questionnaire(
        uuid = UUID.fromString("f92e79df-7f45-4cdf-bebd-e449697083bf"),
        questionnaireType = questionnaireType
    )

    whenever(questionnaireRepository.questionnairesByType(questionnaireType)) doReturn questionnaire

    //when
    testCase.dispatch(LoadQuestionnaireFormEffect(questionnaireType))

    //then
    testCase.assertOutgoingEvents(QuestionnaireFormFetched(questionnaire))
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
