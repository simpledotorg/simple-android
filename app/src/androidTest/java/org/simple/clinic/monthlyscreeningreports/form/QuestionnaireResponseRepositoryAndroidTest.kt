package org.simple.clinic.monthlyscreeningreports.form

import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.rules.LocalAuthenticationRule
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.user.User
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.Rules
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class QuestionnaireResponseRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var repository: QuestionnaireResponseRepository

  @Inject
  lateinit var clock: TestUtcClock

  @Inject
  lateinit var user: User

  @get:Rule
  val rules: RuleChain = Rules
      .global()
      .around(LocalAuthenticationRule())
      .around(SaveDatabaseRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun saving_questionnaire_responses_should_work_correctly() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("e8075335-f766-4605-8216-41bf79189609"),
        name = "PHC Simple"
    )

    val questionnaireResponses = listOf(
        TestData.questionnaireResponse(
            uuid = UUID.fromString("519bc6c3-9bde-4e48-b75b-eb31170b6a48"),
            facilityId = facility.uuid,
        ),
        TestData.questionnaireResponse(
            uuid = UUID.fromString("9058a06a-c793-4951-b339-762107e700fc"),
            facilityId = facility.uuid,
        ),
        TestData.questionnaireResponse(
            uuid = UUID.fromString("9f3c9b1b-c5ad-45cd-9dfb-d73ad34bf328"),
            facilityId = facility.uuid,
        )
    )


    // when
    repository.save(questionnaireResponses)

    // then
    val savedMonthlyScreeningReportsQuestionnaires =
        repository.questionnaireResponsesByType(MonthlyScreeningReports, facility.uuid).blockingFirst()

    Truth.assertThat(savedMonthlyScreeningReportsQuestionnaires).isEqualTo(questionnaireResponses)
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
    repository.save(questionnaireResponse)

    // when
    clock.advanceBy(durationToAdvanceBy)
    repository.updateQuestionnaireResponse(
        user,
        questionnaireResponse.copy(content = mapOf(
            "monthly_screening_reports.outpatient_department_visits" to 6000.0,
            "monthly_screening_reports.blood_pressure_checks_male" to 2700.0,
            "monthly_screening_reports.blood_pressure_checks_female" to 2300.0,
            "monthly_screening_reports.gender" to "Female",
            "monthly_screening_reports.is_smoking" to false
        )))

    // then
    val expected = questionnaireResponse.copy(
        content = mapOf(
            "monthly_screening_reports.outpatient_department_visits" to 6000.0,
            "monthly_screening_reports.blood_pressure_checks_male" to 2700.0,
            "monthly_screening_reports.blood_pressure_checks_female" to 2300.0,
            "monthly_screening_reports.gender" to "Female",
            "monthly_screening_reports.is_smoking" to false
        ),
        timestamps = questionnaireResponse.timestamps.copy(
            updatedAt = questionnaireResponse.timestamps.updatedAt.plus(durationToAdvanceBy)
        ),
        syncStatus = SyncStatus.PENDING,
        lastUpdatedByUserId = user.uuid
    )

    Truth.assertThat(repository.questionnaireResponse(questionnaireResponse.uuid)).isEqualTo(expected)
  }
}
