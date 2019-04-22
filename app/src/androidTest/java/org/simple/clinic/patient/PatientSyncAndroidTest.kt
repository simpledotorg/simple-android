package org.simple.clinic.patient

import androidx.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.patient.sync.PatientPushRequest
import org.simple.clinic.patient.sync.PatientSync
import org.simple.clinic.patient.sync.PatientSyncApiV3
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.sync.BatchSize
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class PatientSyncAndroidTest : BaseSyncCoordinatorAndroidTest<PatientProfile, PatientPayload>() {

  @Inject
  lateinit var repository: PatientRepository

  @Inject
  @field:Named("last_patient_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var sync: PatientSync

  @Inject
  lateinit var syncApi: PatientSyncApiV3

  @Inject
  lateinit var testData: TestData

  private val configProvider = Single.just(SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = BatchSize.VERY_SMALL,
      syncGroup = SyncGroup.FREQUENT))

  private val authenticationRule = AuthenticationRule()

  private val rxErrorsRule = RxErrorsRule()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(authenticationRule)
      .around(rxErrorsRule)!!

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = sync.push()

  override fun pull() = sync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus) = testData.patientProfile(syncStatus = syncStatus)

  override fun generatePayload() = testData.patientPayload()

  override fun lastPullToken(): Preference<Optional<String>> = lastPullToken

  override fun pushNetworkCall(payloads: List<PatientPayload>) = syncApi.push(PatientPushRequest(payloads))

  override fun batchSize(): BatchSize = configProvider.blockingGet().batchSize
}
