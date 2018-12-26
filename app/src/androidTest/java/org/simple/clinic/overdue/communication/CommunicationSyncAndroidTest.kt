package org.simple.clinic.overdue.communication

import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.sync.DataPushResponse
import org.simple.clinic.util.Optional
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class CommunicationSyncAndroidTest : BaseSyncCoordinatorAndroidTest<Communication, CommunicationPayload>() {

  @Inject
  lateinit var repository: CommunicationRepository

  @Inject
  @field:Named("last_communication_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var sync: CommunicationSync

  @Inject
  lateinit var syncApi: CommunicationSyncApiV1

  @Inject
  lateinit var testData: TestData

  @get:Rule
  val authenticationRule = AuthenticationRule(registerAppointmentWithUuid = UUID.randomUUID())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = sync.push()

  override fun pull() = sync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus): Communication {
    return testData.communication(syncStatus = syncStatus)
  }

  override fun generatePayload(): CommunicationPayload {
    return testData.communicationPayload(appointmentUuid = authenticationRule.registerAppointmentWithUuid!!)
  }

  override fun lastPullToken(): Preference<Optional<String>> = lastPullToken

  override fun pushNetworkCall(payloads: List<CommunicationPayload>): Single<DataPushResponse> {
    val request = CommunicationPushRequest(payloads)
    return syncApi.push(request)
  }
}
