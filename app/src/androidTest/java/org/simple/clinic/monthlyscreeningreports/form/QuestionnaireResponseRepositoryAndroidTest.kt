package org.simple.clinic.monthlyscreeningreports.form

import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.Rules
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@Ignore("the qa api is under development")
class QuestionnaireResponseRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var questionnaireResponseRepository: QuestionnaireResponseRepository

  @Inject
  lateinit var clock: TestUtcClock

  @get:Rule
  val rules: RuleChain = Rules
      .global()
      .around(SaveDatabaseRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun saving_questionnaire_responses_should_work_correctly() {
    // given
    val questionnaireResponses = listOf(
        TestData.questionnaireResponse(
            uuid = UUID.fromString("519bc6c3-9bde-4e48-b75b-eb31170b6a48"),
        ),
        TestData.questionnaireResponse(
            uuid = UUID.fromString("9058a06a-c793-4951-b339-762107e700fc"),
        ),
        TestData.questionnaireResponse(
            uuid = UUID.fromString("9f3c9b1b-c5ad-45cd-9dfb-d73ad34bf328"),
        )
    )

    // when
    questionnaireResponseRepository.save(questionnaireResponses)

    // then
    val savedMonthlyReportsQuestionnaires =
        questionnaireResponseRepository.questionnaireResponsesByType(MonthlyScreeningReports)

    Truth.assertThat(savedMonthlyReportsQuestionnaires).isEqualTo(questionnaireResponses)
  }

  @Test
  fun updating_questionnaire_response_should_work_correctly() {
    // given
    val durationToAdvanceBy = Duration.ofMinutes(15L)
    val questionnaireResponse = TestData.questionnaireResponse(
        uuid = UUID.fromString("519bc6c3-9bde-4e48-b75b-eb31170b6a48"),
        createdAt = Instant.now(clock),
        updatedAt = Instant.now(clock),
        syncStatus = SyncStatus.DONE
    )
    questionnaireResponseRepository.save(questionnaireResponse)

    // when
    clock.advanceBy(durationToAdvanceBy)
    questionnaireResponseRepository.updateQuestionnaireResponse(questionnaireResponse.copy(content = mapOf(
        "2000" to "monthly_screening_reports.outpatient_department_visits",
        "2200" to "monthly_screening_reports.blood_pressure_checks_male",
        "2800" to "monthly_screening_reports.blood_pressure_checks_female",
    )))

    // then
    val expected = questionnaireResponse.copy(
        content = mapOf(
            "2000" to "monthly_screening_reports.outpatient_department_visits",
            "2200" to "monthly_screening_reports.blood_pressure_checks_male",
            "2800" to "monthly_screening_reports.blood_pressure_checks_female",
        ),
        timestamps = questionnaireResponse.timestamps.copy(
            updatedAt = questionnaireResponse.timestamps.updatedAt.plus(durationToAdvanceBy)
        ),
        syncStatus = SyncStatus.PENDING
    )

    Truth.assertThat(questionnaireResponseRepository.questionnaireResponse(questionnaireResponse.uuid)).isEqualTo(expected)
  }
}
