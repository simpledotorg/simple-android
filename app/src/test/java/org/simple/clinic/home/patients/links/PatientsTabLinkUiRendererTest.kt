package org.simple.clinic.home.patients.links

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.MonthlySuppliesReports
import org.simple.clinic.questionnaire.QuestionnaireResponseSections
import org.simple.clinic.questionnaire.QuestionnaireSections
import org.simple.sharedTestCode.TestData
import java.util.UUID

class PatientsTabLinkUiRendererTest {
  private val ui = mock<PatientsTabLinkUi>()
  private val defaultModel = PatientsTabLinkModel.default()

  @Test
  fun `when monthlyScreeningReportsEnabled is enabled and data is loaded, then show monthly screening link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        teleconsultationEnabled = false,
        monthlyScreeningReportsEnabled = true,
        monthlySuppliesReportsEnabled = false

    ))

    val questionnaireSections = QuestionnaireSections(
        screeningQuestionnaire = TestData.questionnaire(
            uuid = UUID.fromString("1f7ba287-2dfa-4c10-9547-bc7f4d9b3cf6"),
            questionnaireType = MonthlyScreeningReports
        ),
        suppliesQuestionnaire = null
    )

    val questionnaireResponsesSections = QuestionnaireResponseSections(
        screeningQuestionnaireResponseList = listOf(
            TestData.questionnaireResponse(
                uuid = UUID.fromString("e5ba4172-6c1c-41b5-a38a-51ed9dfbf34e"),
                questionnaireType = MonthlyScreeningReports
            )),
        suppliesQuestionnaireResponseList = listOf(),
    )

    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = false
    )

    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .questionnairesLoaded(questionnaireSections)
            .questionnaireResponsesLoaded(questionnaireResponsesSections)
    )

    // then
    verify(ui).showOrHideLinkView(true)
    verify(ui).showOrHideMonthlyScreeningReportsView(true)
    verify(ui).showOrHideMonthlySuppliesReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when monthly screening report form not loaded, the hide monthly screening link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        teleconsultationEnabled = false,
        monthlyScreeningReportsEnabled = true,
        monthlySuppliesReportsEnabled = false
    ))

    val questionnaireSections = QuestionnaireSections(
        screeningQuestionnaire = null,
        suppliesQuestionnaire = null
    )

    val questionnaireResponsesSections = QuestionnaireResponseSections(
        screeningQuestionnaireResponseList = listOf(
            TestData.questionnaireResponse(
                uuid = UUID.fromString("e5ba4172-6c1c-41b5-a38a-51ed9dfbf34e"),
                questionnaireType = MonthlyScreeningReports
            )),
        suppliesQuestionnaireResponseList = listOf(),
    )

    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = false
    )


    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .questionnairesLoaded(questionnaireSections)
            .questionnaireResponsesLoaded(questionnaireResponsesSections)
    )

    // then
    verify(ui).showOrHideLinkView(false)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHideMonthlySuppliesReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when monthly screening list is not loaded, the hide monthly screening link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        teleconsultationEnabled = false,
        monthlyScreeningReportsEnabled = true,
        monthlySuppliesReportsEnabled = false
    ))

    val questionnaireSections = QuestionnaireSections(
        screeningQuestionnaire = TestData.questionnaire(
            uuid = UUID.fromString("1f7ba287-2dfa-4c10-9547-bc7f4d9b3cf6"),
            questionnaireType = MonthlyScreeningReports
        ),
        suppliesQuestionnaire = null
    )

    val questionnaireResponsesSections = QuestionnaireResponseSections(
        screeningQuestionnaireResponseList = listOf(),
        suppliesQuestionnaireResponseList = listOf(),
    )

    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = false
    )

    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .questionnairesLoaded(questionnaireSections)
            .questionnaireResponsesLoaded(questionnaireResponsesSections)
    )

    // then
    verify(ui).showOrHideLinkView(false)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHideMonthlySuppliesReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when monthlyScreeningReportsEnabled is disabled, then hide monthly screening link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        teleconsultationEnabled = false,
        monthlyScreeningReportsEnabled = false,
        monthlySuppliesReportsEnabled = false
    ))
    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = true
    )


    // when
    uiRenderer.render(defaultModel.currentFacilityLoaded(facility))

    // then
    verify(ui).showOrHideLinkView(true)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHideMonthlySuppliesReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(true)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when monthlySuppliesReportsEnabled is enabled and data is loaded, then show monthly supplies link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        teleconsultationEnabled = false,
        monthlyScreeningReportsEnabled = false,
        monthlySuppliesReportsEnabled = true

    ))

    val questionnaireSections = QuestionnaireSections(
        screeningQuestionnaire = null,
        suppliesQuestionnaire = TestData.questionnaire(
            uuid = UUID.fromString("1f7ba287-2dfa-4c10-9547-bc7f4d9b3cf6"),
            questionnaireType = MonthlySuppliesReports
        )
    )

    val questionnaireResponsesSections = QuestionnaireResponseSections(
        screeningQuestionnaireResponseList = listOf(),
        suppliesQuestionnaireResponseList = listOf(
            TestData.questionnaireResponse(
                uuid = UUID.fromString("e5ba4172-6c1c-41b5-a38a-51ed9dfbf34e"),
                questionnaireType = MonthlySuppliesReports
            )),
    )

    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = false
    )

    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .questionnairesLoaded(questionnaireSections)
            .questionnaireResponsesLoaded(questionnaireResponsesSections)
    )

    // then
    verify(ui).showOrHideLinkView(true)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHideMonthlySuppliesReportsView(true)
    verify(ui).showOrHidePatientLineListDownload(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when monthly supplies report form not loaded, the hide monthly supplies link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        teleconsultationEnabled = false,
        monthlyScreeningReportsEnabled = true,
        monthlySuppliesReportsEnabled = true
    ))

    val questionnaireSections = QuestionnaireSections(
        screeningQuestionnaire = null,
        suppliesQuestionnaire = null
    )

    val questionnaireResponsesSections = QuestionnaireResponseSections(
        screeningQuestionnaireResponseList = listOf(),
        suppliesQuestionnaireResponseList = listOf(
            TestData.questionnaireResponse(
                uuid = UUID.fromString("e5ba4172-6c1c-41b5-a38a-51ed9dfbf34e"),
                questionnaireType = MonthlySuppliesReports
            )),
    )

    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = false
    )


    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .questionnairesLoaded(questionnaireSections)
            .questionnaireResponsesLoaded(questionnaireResponsesSections)
    )

    // then
    verify(ui).showOrHideLinkView(false)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHideMonthlySuppliesReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when monthly supplies list is not loaded, the hide monthly supplies link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        teleconsultationEnabled = false,
        monthlyScreeningReportsEnabled = true,
        monthlySuppliesReportsEnabled = true
    ))

    val questionnaireSections = QuestionnaireSections(
        screeningQuestionnaire = null,
        suppliesQuestionnaire = TestData.questionnaire(
            uuid = UUID.fromString("1f7ba287-2dfa-4c10-9547-bc7f4d9b3cf6"),
            questionnaireType = MonthlySuppliesReports
        )
    )

    val questionnaireResponsesSections = QuestionnaireResponseSections(
        screeningQuestionnaireResponseList = listOf(),
        suppliesQuestionnaireResponseList = listOf(),
    )

    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = false
    )

    // when
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(facility)
            .questionnairesLoaded(questionnaireSections)
            .questionnaireResponsesLoaded(questionnaireResponsesSections)
    )

    // then
    verify(ui).showOrHideLinkView(false)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHideMonthlySuppliesReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when monthlySuppliesReportsEnabled is disabled, then hide monthly screening link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        teleconsultationEnabled = false,
        monthlyScreeningReportsEnabled = false,
        monthlySuppliesReportsEnabled = false
    ))
    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = true
    )

    // when
    uiRenderer.render(defaultModel.currentFacilityLoaded(facility))

    // then
    verify(ui).showOrHideLinkView(true)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHideMonthlySuppliesReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(true)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when monthlyScreeningReportsEnabled, monthlySuppliesReportsEnabled and patient line list download is disabled, then hide link option`() {
    // given
    val facility = TestData.facility(facilityConfig = FacilityConfig(
        diabetesManagementEnabled = false,
        teleconsultationEnabled = false,
        monthlyScreeningReportsEnabled = false,
        monthlySuppliesReportsEnabled = false
    ))
    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = false
    )

    // when
    uiRenderer.render(defaultModel.currentFacilityLoaded(facility))

    // then
    verify(ui).showOrHideLinkView(false)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHideMonthlySuppliesReportsView(false)
    verify(ui).showOrHidePatientLineListDownload(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient line list download is enabled, then show patient line list download option`() {
    // given
    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = true
    )

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showOrHideLinkView(true)
    verify(ui).showOrHidePatientLineListDownload(true)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHideMonthlySuppliesReportsView(false)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient line list download is disabled, then hide patient line list download option`() {
    // given
    val uiRenderer = PatientsTabLinkUiRenderer(
        ui = ui,
        isPatientLineListEnabled = false
    )

    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showOrHideLinkView(false)
    verify(ui).showOrHidePatientLineListDownload(false)
    verify(ui).showOrHideMonthlyScreeningReportsView(false)
    verify(ui).showOrHideMonthlySuppliesReportsView(false)
    verifyNoMoreInteractions(ui)
  }
}
