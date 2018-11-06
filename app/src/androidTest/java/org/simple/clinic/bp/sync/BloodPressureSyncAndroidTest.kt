package org.simple.clinic.bp.sync

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import org.junit.Before
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class BloodPressureSyncAndroidTest : BaseSyncCoordinatorAndroidTest<BloodPressureMeasurement, BloodPressureMeasurementPayload>() {

  @Inject
  lateinit var repository: BloodPressureRepository

  @Inject
  @field:Named("last_bp_pull_timestamp")
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var syncApi: BloodPressureSyncApiV1

  @Inject
  lateinit var bpSync: BloodPressureSync

  @Inject
  lateinit var testData: TestData

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = bpSync.push()

  override fun pull() = bpSync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus) = testData.bloodPressureMeasurement(syncStatus = syncStatus)

  override fun generatePayload() = testData.bpPayload()

  override fun lastPullTimestamp() = lastPullTimestamp

  override fun pushNetworkCall(payloads: List<BloodPressureMeasurementPayload>) = syncApi.push(BloodPressurePushRequest(payloads))
}
