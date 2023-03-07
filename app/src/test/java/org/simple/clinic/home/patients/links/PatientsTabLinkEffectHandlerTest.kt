package org.simple.clinic.home.patients.links

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.QuestionnaireRepository
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
import java.util.UUID

class PatientsTabLinkEffectHandlerTest {
  private val uiActions = mock<PatientsTabLinkUiActions>()
  private val responseRepository = mock<QuestionnaireResponseRepository>()
  private val formRepository = mock<QuestionnaireRepository>()

  private val facility = TestData.facility(
      uuid = UUID.fromString("e8075335-f766-4605-8216-41bf79189609"),
      name = "PHC Simple"
  )

  private val questionnaireResponse = TestData.questionnaireResponse(
      uuid = UUID.fromString("2d180e91-332e-45cf-8817-931176e2c52d"),
      questionnaireType = MonthlyScreeningReports
  )

  private val questionnaire = TestData.questionnaire(
      uuid = UUID.fromString("890aeeb5-d6c5-4a28-ab3c-fca0e193aa6f"),
      questionnaireType = MonthlyScreeningReports
  )

  private val effectHandler = PatientsTabLinkEffectHandler(
      currentFacility = Observable.just(facility),
      questionnaireRepository = formRepository,
      questionnaireResponseRepository = responseRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions,
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load current facility effect is received, then load current facility`() {
    // when
    effectHandlerTestCase.dispatch(LoadCurrentFacility)

    // then
    effectHandlerTestCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load monthly report response list effect is received, then load monthly report response list`() {
    whenever(responseRepository.questionnaireResponsesByType(MonthlyScreeningReports)) doReturn Observable.just(listOf(questionnaireResponse))

    // when
    effectHandlerTestCase.dispatch(LoadMonthlyScreeningReportResponseList)

    // then
    effectHandlerTestCase.assertOutgoingEvents(MonthlyScreeningReportResponseListLoaded(listOf(questionnaireResponse)))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load monthly report form effect is received, then load monthly report form`() {
    whenever(formRepository.questionnairesByType(MonthlyScreeningReports)) doReturn Observable.just(questionnaire)

    // when
    effectHandlerTestCase.dispatch(LoadMonthlyScreeningReportForm)

    // then
    effectHandlerTestCase.assertOutgoingEvents(MonthlyScreeningReportFormLoaded(questionnaire))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open monthly screening report list screen is received, then open the screen`() {
    // when
    effectHandlerTestCase.dispatch(OpenMonthlyScreeningReportsListScreen)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).openMonthlyScreeningReports()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open patient line list download dialog effect is received, then open the dialog`() {
    // when
    effectHandlerTestCase.dispatch(OpenPatientLineListDownloadDialog)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).openPatientLineListDownloadDialog()
    verifyNoMoreInteractions(uiActions)
  }
}
