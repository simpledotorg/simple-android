package org.simple.clinic.medicalhistory

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPayload
import org.simple.clinic.medicalhistory.sync.MedicalHistoryPushRequest
import org.simple.clinic.medicalhistory.sync.MedicalHistorySync
import org.simple.clinic.medicalhistory.sync.MedicalHistorySyncApi
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.RegisterPatientRule
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.sync.DataPushResponse
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.util.Optional
import org.simple.clinic.util.Rules
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named


class MedicalHistorySyncAndroidTest : BaseSyncCoordinatorAndroidTest<MedicalHistory, MedicalHistoryPayload>() {

  @Inject
  @Named("last_medicalhistory_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var sync: MedicalHistorySync

  @Inject
  lateinit var syncApi: MedicalHistorySyncApi

  @Inject
  lateinit var repository: MedicalHistoryRepository

  @Inject
  lateinit var testData: TestData

  private val configProvider = Single.just(SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = 10,
      syncGroup = SyncGroup.FREQUENT))

  private val registerPatientRule = RegisterPatientRule(patientUuid = UUID.randomUUID())

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())
      .around(registerPatientRule)

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
        patientUuid = registerPatientRule.patientUuid,
        // This is manually being set here because the server endpoint does not accept `Unanswered`
        // as a valid value yet, and the test factory can end up setting the value to `Unanswered`,
        // which causes the test to fail.
        diagnosedWithHypertension = listOf(Yes, No).shuffled().first()
    )
  }

  override fun generatePayload(): MedicalHistoryPayload {
    return testData.medicalHistoryPayload(
        patientUuid = registerPatientRule.patientUuid,
        // This is manually being set here because the server endpoint does not accept `Unanswered`
        // as a valid value yet, and the test factory can end up setting the value to `Unanswered`,
        // which causes the test to fail.
        hasHypertension = listOf(Yes, No).shuffled().first()
    )
  }

  override fun lastPullToken(): Preference<Optional<String>> = lastPullToken

  override fun pushNetworkCall(payloads: List<MedicalHistoryPayload>): Single<DataPushResponse> {
    val request = MedicalHistoryPushRequest(payloads)
    return syncApi.push(request)
  }

  override fun batchSize(): Int = configProvider.blockingGet().batchSize
}
