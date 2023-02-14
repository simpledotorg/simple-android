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
import org.simple.clinic.main.TypedPreference.Type.LastQuestionnaireResponsePullToken
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.questionnaireresponse.sync.QuestionnaireResponseSync
import org.simple.clinic.questionnaireresponse.sync.QuestionnaireResponseSyncApi
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.user.UserSession
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
  lateinit var repository: QuestionnaireResponseRepository

  @Inject
  @TypedPreference(LastQuestionnaireResponsePullToken)
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: QuestionnaireResponseSyncApi

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var syncInterval: SyncInterval

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())
      .around(SaveDatabaseRule())

  private lateinit var sync: QuestionnaireResponseSync

  private val batchSize = 3
  private lateinit var config: SyncConfig

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

    sync = QuestionnaireResponseSync(
        syncCoordinator = SyncCoordinator(),
        api = syncApi,
        repository = repository,
        lastPullToken = lastPullToken,
        config = config
    )
  }

  private fun resetLocalData() {
    clearQuestionnaireResponseData()
    lastPullToken.delete()
  }

  private fun clearQuestionnaireResponseData() {
    appDatabase.questionnaireResponseDao().clear()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // given
    val totalNumberOfRecords = batchSize * 2 + 1
    val records = (1..totalNumberOfRecords).map {
      val uuid = UUID.randomUUID()
      val questionnaireUuid = UUID.randomUUID()
      TestData.questionnaireResponse(
          uuid = uuid,
          questionnaireId = questionnaireUuid,
          syncStatus = SyncStatus.PENDING
      )
    }
    Truth.assertThat(records).containsNoDuplicates()

    repository.save(records)
    Truth.assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(totalNumberOfRecords)

    // when
    sync.push()
    clearQuestionnaireResponseData()
    sync.pull()

    // then
    val expectedPulledRecords = records.map { it.syncCompleted() }
    val pulledRecords = repository.recordsWithSyncStatus(SyncStatus.DONE)

    Truth.assertThat(pulledRecords).containsAtLeastElementsIn(expectedPulledRecords)
  }

  private fun QuestionnaireResponse.syncCompleted(): QuestionnaireResponse = copy(syncStatus = SyncStatus.DONE)
}
