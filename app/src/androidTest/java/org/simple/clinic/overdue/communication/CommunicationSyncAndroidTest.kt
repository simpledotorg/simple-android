package org.simple.clinic.overdue.communication

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.junit.Before
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.BaseDataSyncAndroidTest
import org.simple.clinic.sync.DataPushResponse
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class CommunicationSyncAndroidTest: BaseDataSyncAndroidTest<Communication, CommunicationPayload>() {

  @Inject
  lateinit var repository: CommunicationRepository

  @Inject
  @field:Named("last_communication_pull_timestamp")
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var sync: CommunicationSync

  @Inject
  lateinit var syncApi: CommunicationSyncApiV1

  @Inject
  lateinit var testData: TestData

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = sync.push()

  override fun pull() = sync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus) = testData.communication(syncStatus)

  override fun generatePayload() = testData.communicationPayload()

  override fun lastPullTimestamp() = lastPullTimestamp

  override fun pushNetworkCall(payloads: List<CommunicationPayload>): Single<DataPushResponse> {
    val request = CommunicationPushRequest(payloads)
    return syncApi.push(request)
  }
}
