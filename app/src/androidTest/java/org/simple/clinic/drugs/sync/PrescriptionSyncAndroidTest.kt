package org.simple.clinic.drugs.sync

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
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.sync.BatchSize
import org.simple.clinic.sync.RegisterPatientRule
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class PrescriptionSyncAndroidTest : BaseSyncCoordinatorAndroidTest<PrescribedDrug, PrescribedDrugPayload>() {

  @Inject
  lateinit var repository: PrescriptionRepository

  @Inject
  @field:Named("last_prescription_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: PrescriptionSyncApi

  @Inject
  lateinit var prescriptionSync: PrescriptionSync

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var facilityRepository: FacilityRepository

  private val configProvider = Single.just(SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = BatchSize.VERY_SMALL,
      syncGroup = SyncGroup.FREQUENT))

  private val user: User
    get() = userSession.loggedInUserImmediate()!!

  private val currentFacilityUuid: UUID
    get() = facilityRepository.currentFacilityUuid(user)!!

  private val authenticationRule = AuthenticationRule()

  private val registerPatientRule = RegisterPatientRule(UUID.randomUUID())

  private val rxErrorsRule = RxErrorsRule()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(authenticationRule)
      .around(registerPatientRule)
      .around(rxErrorsRule)!!

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = prescriptionSync.push()

  override fun pull() = prescriptionSync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus): PrescribedDrug {
    return testData.prescription(
        syncStatus = syncStatus,
        patientUuid = registerPatientRule.patientUuid,
        facilityUuid = currentFacilityUuid)
  }

  override fun generatePayload(): PrescribedDrugPayload {
    return testData.prescriptionPayload(
        patientUuid = registerPatientRule.patientUuid,
        facilityUuid = currentFacilityUuid)
  }

  override fun lastPullToken(): Preference<Optional<String>> = lastPullToken

  override fun pushNetworkCall(payloads: List<PrescribedDrugPayload>) = syncApi.push(PrescriptionPushRequest(payloads))

  override fun batchSize(): BatchSize = configProvider.blockingGet().batchSize
}
