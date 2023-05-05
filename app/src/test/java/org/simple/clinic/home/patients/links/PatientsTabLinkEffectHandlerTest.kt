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
import org.simple.clinic.questionnaire.MonthlySuppliesReports
import org.simple.clinic.questionnaire.QuestionnaireRepository
import org.simple.clinic.questionnaire.QuestionnaireResponseSections
import org.simple.clinic.questionnaire.QuestionnaireSections
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

  private val screeningForm = TestData.questionnaire(
      uuid = UUID.fromString("890aeeb5-d6c5-4a28-ab3c-fca0e193aa6f"),
      questionnaireType = MonthlyScreeningReports
  )

  private val suppliesForm = TestData.questionnaire(
      uuid = UUID.fromString("e51994cd-e4ae-41b3-872e-4c9a3d109d73"),
      questionnaireType = MonthlySuppliesReports
  )

  private val screeningResponse = TestData.questionnaireResponse(
      uuid = UUID.fromString("b461a17a-17f6-4749-93f7-fb90f4a254fa"),
      questionnaireType = MonthlyScreeningReports
  )

  private val suppliesResponse = TestData.questionnaireResponse(
      uuid = UUID.fromString("9f61d80d-530b-478c-bffd-c0db914d2321"),
      questionnaireType = MonthlySuppliesReports
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
  fun `when load questionnaires effect is received, then load questionnaires`() {
    whenever(formRepository.questionnaires()) doReturn Observable.just(listOf(screeningForm, suppliesForm))

    // when
    effectHandlerTestCase.dispatch(LoadQuestionnaires)

    // then
    effectHandlerTestCase.assertOutgoingEvents(QuestionnairesLoaded(QuestionnaireSections(screeningForm, suppliesForm)))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load questionnaires responses effect is received, then load questionnaires responses`() {
    whenever(responseRepository.questionnaireResponsesInFacility(facility.uuid)) doReturn Observable.just(listOf(screeningResponse, suppliesResponse))

    // when
    effectHandlerTestCase.dispatch(LoadQuestionnairesResponses)

    // then
    effectHandlerTestCase.assertOutgoingEvents(QuestionnairesResponsesLoaded(QuestionnaireResponseSections(listOf(screeningResponse), listOf(suppliesResponse))))
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
