package org.simple.clinic.drugs.sync

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.util.Optional
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
  lateinit var syncApi: PrescriptionSyncApiV1

  @Inject
  lateinit var prescriptionSync: PrescriptionSync

  @Inject
  lateinit var testData: TestData

  @get:Rule
  val authenticationRule = AuthenticationRule(registerPatientWithUuid = UUID.randomUUID())

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
        patientUuid = authenticationRule.registerPatientWithUuid!!,
        facilityUuid = authenticationRule.registeredFacilityUuid!!)
  }

  override fun generatePayload(): PrescribedDrugPayload {
    return testData.prescriptionPayload(
        patientUuid = authenticationRule.registerPatientWithUuid!!,
        facilityUuid = authenticationRule.registeredFacilityUuid!!)
  }

  override fun lastPullToken(): Preference<Optional<String>> = lastPullToken

  override fun pushNetworkCall(payloads: List<PrescribedDrugPayload>) = syncApi.push(PrescriptionPushRequest(payloads))
}
