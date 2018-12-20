package org.simple.clinic.medicalhistory

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPushRequest
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.medicalhistory.sync.MedicalHistorySyncApiV2
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.sync.DataPushResponse
import org.simple.clinic.util.Optional
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class MedicalHistorySyncAndroidTest : BaseSyncCoordinatorAndroidTest<MedicalHistory, MedicalHistoryPayload>() {

  @Inject
  @field:Named("last_medicalhistory_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var sync: MedicalHistorySync

  @Inject
  lateinit var syncApi: MedicalHistorySyncApiV2

  @Inject
  lateinit var repository: MedicalHistoryRepository

  @Inject
  lateinit var testData: TestData

  @get:Rule
  val authenticationRule = AuthenticationRule(registerPatientWithUuid = UUID.randomUUID())

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = sync.push()

  override fun pull() = sync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus): MedicalHistory {
    return testData.medicalHistory(
        syncStatus = syncStatus,
        patientUuid = authenticationRule.registerPatientWithUuid!!)
  }

  override fun generatePayload(): MedicalHistoryPayload {
    return testData.medicalHistoryPayload(patientUuid = authenticationRule.registerPatientWithUuid!!)
  }

  override fun lastPullToken(): Preference<Optional<String>> = lastPullToken

  override fun pushNetworkCall(payloads: List<MedicalHistoryPayload>): Single<DataPushResponse> {
    val request = MedicalHistoryPushRequest(payloads)
    return syncApi.push(request)
  }
}
