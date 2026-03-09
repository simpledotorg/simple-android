package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.returnscore.ReturnScoreRepository
import org.simple.clinic.returnscore.sync.ReturnScoreSync
import org.simple.clinic.returnscore.sync.ReturnScoreSyncApi
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.util.Rules
import java.util.Optional
import javax.inject.Inject

class ReturnScoreSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var repository: ReturnScoreRepository

  @Inject
  @TypedPreference(TypedPreference.Type.LastReturnScorePullToken)
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: ReturnScoreSyncApi

  @Inject
  lateinit var syncInterval: SyncInterval

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())
      .around(SaveDatabaseRule())

  private lateinit var sync: ReturnScoreSync

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

    sync = ReturnScoreSync(
        syncCoordinator = SyncCoordinator(),
        api = syncApi,
        repository = repository,
        lastPullToken = lastPullToken,
        config = config
    )
  }

  private fun resetLocalData() {
    clearReturnScoreDao()
    lastPullToken.delete()
  }

  private fun clearReturnScoreDao() {
    appDatabase.returnScoreDao().clear()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // when
    Truth.assertThat(repository.recordCount().blockingFirst()).isEqualTo(0)
    sync.pull()

    // then
    val pulledRecords = repository.returnScoresImmediate()

    Truth.assertThat(pulledRecords).isNotEmpty()
  }
}
