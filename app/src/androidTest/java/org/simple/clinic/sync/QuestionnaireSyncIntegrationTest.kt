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
import org.simple.clinic.questionnaire.QuestionnaireRepository
import org.simple.clinic.questionnaire.sync.QuestionnaireSync
import org.simple.clinic.questionnaire.sync.QuestionnaireSyncApi
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.sharedTestCode.util.Rules
import java.util.Optional
import javax.inject.Inject

@Ignore("the review env doesn't contain any data")
class QuestionnaireSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var repository: QuestionnaireRepository

  @Inject
  @TypedPreference(LastQuestionnairePullToken)
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: QuestionnaireSyncApi

  @Inject
  lateinit var syncInterval: SyncInterval

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())
      .around(SaveDatabaseRule())

  private lateinit var sync: QuestionnaireSync

  private val batchSize = 1000
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

    sync = QuestionnaireSync(
        syncCoordinator = SyncCoordinator(),
        api = syncApi,
        repository = repository,
        lastPullToken = lastPullToken,
        config = config
    )
  }

  private fun resetLocalData() {
    clearQuestionnaireData()
    lastPullToken.delete()
  }

  private fun clearQuestionnaireData() {
    appDatabase.questionnaireDao().clear()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // when
    Truth.assertThat(repository.recordCount().blockingFirst()).isEqualTo(0)
    sync.pull()

    // then
    val pulledRecords = repository.questionnairesImmediate()

    Truth.assertThat(pulledRecords).isNotEmpty()
  }
}
