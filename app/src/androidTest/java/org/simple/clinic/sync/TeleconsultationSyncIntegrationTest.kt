package org.simple.clinic.sync

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultFacilityInfoApi
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationSync
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Rules
import javax.inject.Inject

@Ignore("API endpoints are not yet ready")
class TeleconsultationSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var repository: TeleconsultationFacilityRepository

  @Inject
  lateinit var syncApi: TeleconsultFacilityInfoApi

  @Inject
  lateinit var userSession: UserSession

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  private lateinit var sync: TeleconsultationSync

  private val batchSize = 3
  private val config = SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = batchSize,
      syncGroup = SyncGroup.FREQUENT
  )

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    resetLocalData()

    sync = TeleconsultationSync(
        repository = repository,
        api = syncApi,
        config = config
    )
  }

  @After
  fun tearDown() {
    resetLocalData()
  }

  private fun resetLocalData() {
    clearTeleconsultFacilityData()
  }

  private fun clearTeleconsultFacilityData() {
    appDatabase.teleconsultFacilityWithMedicalOfficersDao().clear()
    appDatabase.teleconsultFacilityInfoDao().clear()
    appDatabase.teleconsultMedicalOfficersDao().clear()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // when
    assertThat(repository.recordCount().blockingFirst()).isEqualTo(0)
    sync.pull()

    // then
    val pulledRecords = repository.recordsWithSyncStatus(SyncStatus.DONE)

    assertThat(pulledRecords).isNotEmpty()
  }
}
