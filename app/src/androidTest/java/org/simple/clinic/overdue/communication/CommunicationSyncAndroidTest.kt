package org.simple.clinic.overdue.communication

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
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentPushRequest
import org.simple.clinic.overdue.AppointmentSyncApiV2
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.sync.DataPushResponse
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
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
  lateinit var syncApi: CommunicationSyncApiV2

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var appointmentSyncApi: AppointmentSyncApiV2

  @Inject
  lateinit var configProvider: Single<SyncConfig>

  private val authenticationRule = AuthenticationRule()

  private val rxErrorsRule = RxErrorsRule()

  @get:Rule
  val ruleChain = RuleChain
      .outerRule(authenticationRule)
      .around(rxErrorsRule)!!

  val appointmentUuid = UUID.randomUUID()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    registerAppointment()
  }

  private fun registerAppointment() {
    val registeredFacilityUuid = facilityRepository
        .currentFacilityUuid(userSession.loggedInUserImmediate()!!)!!

    val appointmentPayload = testData.appointmentPayload(uuid = appointmentUuid, apiV2Enabled = true, facilityUuid = registeredFacilityUuid)
    val pushRequest = AppointmentPushRequest(listOf(appointmentPayload))

    appointmentSyncApi
        .push(pushRequest)
        .blockingGet()
  }

  override fun push() = sync.push()

  override fun pull() = sync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus): Communication {
    return testData.communication(
        syncStatus = syncStatus,
        appointmentUuid = appointmentUuid)
  }

  override fun generatePayload(): CommunicationPayload {
    return testData.communicationPayload(appointmentUuid = appointmentUuid)
  }

  override fun lastPullToken(): Preference<Optional<String>> = lastPullToken

  override fun pushNetworkCall(payloads: List<CommunicationPayload>): Single<DataPushResponse> {
    val request = CommunicationPushRequest(payloads)
    return syncApi.push(request)
  }

  override fun batchSize() = configProvider.blockingGet().batchSize
}
