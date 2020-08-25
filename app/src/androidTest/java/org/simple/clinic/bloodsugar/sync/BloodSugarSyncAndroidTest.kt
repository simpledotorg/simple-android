package org.simple.clinic.bloodsugar.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.util.Rules
import javax.inject.Inject
import javax.inject.Named


class BloodSugarSyncAndroidTest : BaseSyncCoordinatorAndroidTest<BloodSugarMeasurement, BloodSugarMeasurementPayload>() {

  @Inject
  lateinit var repository: BloodSugarRepository

  @Inject
  @Named("last_blood_sugar_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: BloodSugarSyncApi

  @Inject
  lateinit var bloodSugarSync: BloodSugarSync

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var user: User

  @Inject
  lateinit var facility: Facility

  private val configProvider = Single.just(SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = 10,
      syncGroup = SyncGroup.FREQUENT))

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = bloodSugarSync.push()

  override fun pull() = bloodSugarSync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus) = testData.bloodSugarMeasurement(
      syncStatus = syncStatus,
      userUuid = user.uuid,
      facilityUuid = facility.uuid
  )

  // TODO (SM): Remove blood sugar type once HbA1c sync is enabled
  override fun generatePayload() = testData.bloodSugarPayload(
      bloodSugarType = Random,
      userUuid = user.uuid,
      facilityUuid = facility.uuid
  )

  override fun lastPullToken(): Preference<Optional<String>> = lastPullToken

  override fun pushNetworkCall(payloads: List<BloodSugarMeasurementPayload>) = syncApi.push(BloodSugarPushRequest(payloads))

  override fun batchSize(): Int = configProvider.blockingGet().batchSize
}
