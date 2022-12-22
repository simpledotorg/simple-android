package org.simple.clinic.monthlyReports.questionnaire

import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.Rules
import java.util.UUID
import javax.inject.Inject

class QuestionnaireRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var questionnaireRepository: QuestionnaireRepository

  @get:Rule
  val rules: RuleChain = Rules
      .global()
      .around(SaveDatabaseRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun saving_questionnaires_should_work_correctly() {
    // given
    val questionnaires = listOf(
        TestData.questionnaire(
            uuid = UUID.fromString("ef5b7656-a6df-459c-a5b0-80d100721597"),
            questionnaireType = MonthlyScreeningReports,
            layout = "Test Questionnaire 1"
        )
    )

    // when
    questionnaireRepository.save(questionnaires)

    // then
    val savedQuestionnaires = questionnaireRepository.questionnaires()

    Truth.assertThat(savedQuestionnaires).isEqualTo(questionnaires)
  }

  @Test
  fun saving_questionnaire_should_delete_existing_entries_with_same_questionnaire_type_if_exist() {
    // given
    val questionnaires = listOf(
        TestData.questionnaire(
            uuid = UUID.fromString("e445737a-8adf-496d-9f0f-5af5cec58446"),
            questionnaireType = MonthlyScreeningReports,
            layout = "Test Questionnaire 1"
        )
    )

    questionnaireRepository.save(questionnaires)

    val newMonthlyScreeningQuestionnaire = TestData.questionnaire(
        uuid = UUID.fromString("9b72f35a-05cd-4d13-82cd-73fcc3dd738b"),
        questionnaireType = MonthlyScreeningReports,
        layout = "Test Questionnaire 2"
    )

    // when
    questionnaireRepository.save(listOf(newMonthlyScreeningQuestionnaire))

    // then
    val monthlyScreeningQuestionnaire = questionnaireRepository.questionnairesByType(MonthlyScreeningReports)

    Truth.assertThat(monthlyScreeningQuestionnaire).isEqualTo(newMonthlyScreeningQuestionnaire)
  }
}
