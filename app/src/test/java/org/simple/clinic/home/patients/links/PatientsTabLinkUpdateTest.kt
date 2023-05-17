package org.simple.clinic.home.patients.links

import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.junit.Test
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.MonthlySuppliesReports
import org.simple.clinic.questionnaire.QuestionnaireResponseSections
import org.simple.clinic.questionnaire.QuestionnaireSections
import org.simple.sharedTestCode.TestData
import java.util.UUID

class PatientsTabLinkUpdateTest {
  private val defaultModel = PatientsTabLinkModel.default()
  private val updateSpec = UpdateSpec(PatientsTabLinkUpdate())

  @Test
  fun `when current facility is loaded, then update the model`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("7dc68c5a-952d-46e7-83b1-070ce3d32600")
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasModel(defaultModel.currentFacilityLoaded(facility)),
            NextMatchers.hasNoEffects()
        ))
  }

  @Test
  fun `when questionnaire responses are loaded, then update the model`() {
    val questionnaireResponsesSections = QuestionnaireResponseSections(
        screeningQuestionnaireResponseList = listOf(
            TestData.questionnaireResponse(
                uuid = UUID.fromString("e5ba4172-6c1c-41b5-a38a-51ed9dfbf34e"),
                questionnaireType = MonthlyScreeningReports
            )),
        suppliesQuestionnaireResponseList = listOf(
            TestData.questionnaireResponse(
                uuid = UUID.fromString("ffb6d991-b979-4416-9a0c-41957ae8fcf6"),
                questionnaireType = MonthlySuppliesReports
            )
        ),
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(QuestionnaireResponsesLoaded(questionnaireResponsesSections))
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasModel(defaultModel.questionnairesResponsesLoaded(questionnaireResponsesSections)),
            NextMatchers.hasNoEffects()
        ))
  }

  @Test
  fun `when monthly screening report form is loaded, then update the model`() {
    val questionnaireSections = QuestionnaireSections(
        screeningQuestionnaire = TestData.questionnaire(
            uuid = UUID.fromString("1f7ba287-2dfa-4c10-9547-bc7f4d9b3cf6"),
            questionnaireType = MonthlyScreeningReports
        ),
        suppliesQuestionnaire = TestData.questionnaire(
            uuid = UUID.fromString("bb2ecd58-2a1d-4951-9af6-c470dfca45b1"),
            questionnaireType = MonthlySuppliesReports
        )
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(QuestionnairesLoaded(questionnaireSections))
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasModel(defaultModel.questionnairesLoaded(questionnaireSections)),
            NextMatchers.hasNoEffects()
        ))
  }

  @Test
  fun `when monthly screening report list button is clicked, then open monthly screening report list screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(MonthlyScreeningReportsClicked)
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasNoModel(),
            NextMatchers.hasEffects(OpenMonthlyScreeningReportsListScreen)
        ))
  }

  @Test
  fun `when patient line list download button is clicked, then open patient line list download dialog`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DownloadPatientLineListClicked())
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasNoModel(),
            NextMatchers.hasEffects(OpenPatientLineListDownloadDialog)
        ))
  }
}
