package org.simple.clinic.medicalhistory

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.junit.Before
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPushRequest
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.medicalhistory.sync.MedicalHistorySyncApiV1
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.BaseDataSyncAndroidTest
import org.simple.clinic.sync.DataPushResponse
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class MedicalHistorySyncAndroidTest : BaseDataSyncAndroidTest<MedicalHistory, MedicalHistoryPayload>() {

  @Inject
  @field:Named("last_medicalhistory_pull_timestamp")
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var sync: MedicalHistorySync

  @Inject
  lateinit var syncApi: MedicalHistorySyncApiV1

  @Inject
  lateinit var repository: MedicalHistoryRepository

  @Inject
  lateinit var testData: TestData

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = sync.push()

  override fun pull() = sync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus) = testData.medicalHistory(syncStatus)

  override fun generatePayload() = testData.medicalHistoryPayload()

  override fun lastPullTimestamp() = lastPullTimestamp

  override fun pushNetworkCall(payloads: List<MedicalHistoryPayload>): Single<DataPushResponse> {
    val request = MedicalHistoryPushRequest(payloads)
    return syncApi.push(request)
  }
}
