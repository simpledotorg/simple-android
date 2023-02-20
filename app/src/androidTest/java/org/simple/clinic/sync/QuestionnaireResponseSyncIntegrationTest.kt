package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastQuestionnairePullToken
import org.simple.clinic.main.TypedPreference.Type.LastQuestionnaireResponsePullToken
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaire.QuestionnaireRepository
import org.simple.clinic.questionnaire.sync.QuestionnaireSync
import org.simple.clinic.questionnaire.sync.QuestionnaireSyncApi
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.questionnaireresponse.sync.QuestionnaireResponseSync
import org.simple.clinic.questionnaireresponse.sync.QuestionnaireResponseSyncApi
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.unsafeLazy
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.Rules
import java.util.Optional
import java.util.UUID
import javax.inject.Inject

@Ignore("the qa api is under development")
class QuestionnaireResponseSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var questionnaireRepository: QuestionnaireRepository

  @Inject
  lateinit var questionnaireResponseRepository: QuestionnaireResponseRepository

  @Inject
  @TypedPreference(LastQuestionnairePullToken)
  lateinit var questionnaireLastPullToken: Preference<Optional<String>>

  @Inject
  @TypedPreference(LastQuestionnaireResponsePullToken)
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var questionnaireSyncApi: QuestionnaireSyncApi

  @Inject
  lateinit var questionnaireResponseSyncApi: QuestionnaireResponseSyncApi

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var syncInterval: SyncInterval

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())
      .around(SaveDatabaseRule())

  private lateinit var questionnaireSync: QuestionnaireSync

  private lateinit var questionnaireResponseSync: QuestionnaireResponseSync

  private val batchSize = 3
  private lateinit var config: SyncConfig

  private val currentFacilityUuid: UUID by unsafeLazy { userSession.loggedInUserImmediate()!!.currentFacilityUuid }

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    resetLocalData()

    config = SyncConfig(
        syncInterval = syncInterval,
        pullBatchSize = batchSize,
        pushBatchSize = batchSize,
        name = ""
    )

    questionnaireSync = QuestionnaireSync(
        syncCoordinator = SyncCoordinator(),
        api = questionnaireSyncApi,
        repository = questionnaireRepository,
        lastPullToken = questionnaireLastPullToken,
        config = config
    )

    questionnaireResponseSync = QuestionnaireResponseSync(
        syncCoordinator = SyncCoordinator(),
        api = questionnaireResponseSyncApi,
        repository = questionnaireResponseRepository,
        lastPullToken = lastPullToken,
        config = config
    )
  }

  private fun resetLocalData() {
    clearQuestionnaireData()
    clearQuestionnaireResponseData()
    lastPullToken.delete()
    questionnaireLastPullToken.delete()
  }

  private fun clearQuestionnaireData() {
    appDatabase.questionnaireDao().clearData()
  }

  private fun clearQuestionnaireResponseData() {
    appDatabase.questionnaireResponseDao().clear()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // given
    val totalNumberOfRecords = batchSize * 2 + 1

    questionnaireSync.pull()
    val monthlyScreeningReportQuestionnaire = questionnaireRepository.questionnairesByType(MonthlyScreeningReports)

    Truth.assertThat(monthlyScreeningReportQuestionnaire).isNotNull()

    val records = (1..totalNumberOfRecords).map {
      val uuid = UUID.randomUUID()
      TestData.questionnaireResponse(
          uuid = uuid,
          questionnaireId = monthlyScreeningReportQuestionnaire.uuid,
          questionnaireType = monthlyScreeningReportQuestionnaire.questionnaire_type,
          facilityId = currentFacilityUuid,
          syncStatus = SyncStatus.PENDING
      )
    }
    Truth.assertThat(records).containsNoDuplicates()

    questionnaireResponseRepository.save(records)
    Truth.assertThat(questionnaireResponseRepository.pendingSyncRecordCount().blockingFirst()).isEqualTo(totalNumberOfRecords)

    // when
    questionnaireResponseSync.push()
    clearQuestionnaireResponseData()
    questionnaireResponseSync.pull()

    // then
    val expectedPulledRecords = records.map { it.syncCompleted() }
    val pulledRecords = questionnaireResponseRepository.recordsWithSyncStatus(SyncStatus.DONE)

    Truth.assertThat(pulledRecords).containsAtLeastElementsIn(expectedPulledRecords)
  }

  private fun QuestionnaireResponse.syncCompleted(): QuestionnaireResponse = copy(syncStatus = SyncStatus.DONE)
}
