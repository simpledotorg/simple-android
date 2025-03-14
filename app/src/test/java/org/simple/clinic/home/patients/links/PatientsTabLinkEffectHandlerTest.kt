package org.simple.clinic.home.patients.links

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.questionnaire.DrugStockReports
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.MonthlySuppliesReports
import org.simple.clinic.questionnaire.QuestionnaireRepository
import org.simple.clinic.questionnaire.QuestionnaireResponseSections
import org.simple.clinic.questionnaire.QuestionnaireSections
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.TestData
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

  private val drugStockReportsForm = TestData.questionnaire(
      uuid = UUID.fromString("bfc7c50a-dd2a-4b7a-8093-0ed91633d0ec"),
      questionnaireType = DrugStockReports
  )

  private val screeningResponse = TestData.questionnaireResponse(
      uuid = UUID.fromString("b461a17a-17f6-4749-93f7-fb90f4a254fa"),
      questionnaireType = MonthlyScreeningReports
  )

  private val suppliesResponse = TestData.questionnaireResponse(
      uuid = UUID.fromString("9f61d80d-530b-478c-bffd-c0db914d2321"),
      questionnaireType = MonthlySuppliesReports
  )

  private val drugStockResponse = TestData.questionnaireResponse(
      uuid = UUID.fromString("01068b0c-f940-4873-8b73-c077a576c522"),
      questionnaireType = DrugStockReports
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
    whenever(formRepository.questionnaires()) doReturn Observable.just(listOf(screeningForm, suppliesForm, drugStockReportsForm))

    // when
    effectHandlerTestCase.dispatch(LoadQuestionnaires)

    // then
    effectHandlerTestCase.assertOutgoingEvents(QuestionnairesLoaded(QuestionnaireSections(screeningForm, suppliesForm, drugStockReportsForm)))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load questionnaires responses effect is received, then load questionnaires responses`() {
    whenever(responseRepository.questionnaireResponsesInFacility(facility.uuid)) doReturn Observable.just(listOf(screeningResponse, suppliesResponse, drugStockResponse))

    // when
    effectHandlerTestCase.dispatch(LoadQuestionnaireResponses)

    // then
    effectHandlerTestCase.assertOutgoingEvents(QuestionnaireResponsesLoaded(QuestionnaireResponseSections(listOf(screeningResponse), listOf(suppliesResponse), listOf(drugStockResponse))))
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

  @Test
  fun `when open drug stock reports is clicked, then open the drug stock reports screen`() {
    // when
    effectHandlerTestCase.dispatch(OpenDrugStockReportsScreen)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).openDrugStockReports()
    verifyNoMoreInteractions(uiActions)
  }
}
