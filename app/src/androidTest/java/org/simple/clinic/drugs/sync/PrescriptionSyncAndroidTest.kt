package org.simple.clinic.drugs.sync

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import org.junit.Before
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class PrescriptionSyncAndroidTest : BaseSyncCoordinatorAndroidTest<PrescribedDrug, PrescribedDrugPayload>() {

  @Inject
  lateinit var repository: PrescriptionRepository

  @Inject
  @field:Named("last_prescription_pull_timestamp")
  lateinit var lastPullTimestamp: Preference<Optional<Instant>>

  @Inject
  lateinit var syncApi: PrescriptionSyncApiV1

  @Inject
  lateinit var prescriptionSync: PrescriptionSync

  @Inject
  lateinit var testData: TestData

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = prescriptionSync.push()

  override fun pull() = prescriptionSync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus) = testData.prescription(syncStatus = syncStatus)

  override fun generatePayload() = testData.prescriptionPayload()

  override fun lastPullTimestamp() = lastPullTimestamp

  override fun pushNetworkCall(payloads: List<PrescribedDrugPayload>) = syncApi.push(PrescriptionPushRequest(payloads))
}
