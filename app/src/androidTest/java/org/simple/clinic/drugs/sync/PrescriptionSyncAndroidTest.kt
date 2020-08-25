package org.simple.clinic.drugs.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.RegisterPatientRule
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.sync.BatchSize
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import org.simple.clinic.util.Rules
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named


class PrescriptionSyncAndroidTest : BaseSyncCoordinatorAndroidTest<PrescribedDrug, PrescribedDrugPayload>() {

  @Inject
  lateinit var repository: PrescriptionRepository

  @Inject
  @Named("last_prescription_pull_token")
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

  private val currentFacilityUuid: UUID
    get() = facilityRepository.currentFacilityUuid()!!

  private val registerPatientRule = RegisterPatientRule(UUID.randomUUID())

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())
      .around(registerPatientRule)

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
