package org.simple.clinic.bp.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
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


class BloodPressureSyncAndroidTest : BaseSyncCoordinatorAndroidTest<BloodPressureMeasurement, BloodPressureMeasurementPayload>() {

  @Inject
  lateinit var repository: BloodPressureRepository

  @Inject
  @Named("last_bp_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: BloodPressureSyncApi

  @Inject
  lateinit var bpSync: BloodPressureSync

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

  override fun push() = bpSync.push()

  override fun pull() = bpSync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus) = testData.bloodPressureMeasurement(
      syncStatus = syncStatus,
      userUuid = user.uuid,
      facilityUuid = facility.uuid
  )

  override fun generatePayload() = testData.bpPayload(
      userUuid = user.uuid,
      facilityUuid = facility.uuid
  )

  override fun lastPullToken(): Preference<Optional<String>> = lastPullToken

  override fun pushNetworkCall(payloads: List<BloodPressureMeasurementPayload>) = syncApi.push(BloodPressurePushRequest(payloads))

  override fun batchSize(): Int = configProvider.blockingGet().batchSize
}
