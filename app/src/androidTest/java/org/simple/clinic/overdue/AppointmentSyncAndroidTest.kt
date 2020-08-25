package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.sync.BaseSyncCoordinatorAndroidTest
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncGroup
import org.simple.clinic.sync.SyncInterval
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.util.Rules
import javax.inject.Inject
import javax.inject.Named


class AppointmentSyncAndroidTest : BaseSyncCoordinatorAndroidTest<Appointment, AppointmentPayload>() {

  @Inject
  lateinit var repository: AppointmentRepository

  @Inject
  @Named("last_appointment_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var sync: AppointmentSync

  @Inject
  lateinit var syncApi: AppointmentSyncApi

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var user: User

  @Inject
  lateinit var facility: Facility

  private val configProvider = Single.just(SyncConfig(
      syncInterval = SyncInterval.FREQUENT,
      batchSize = 10,
      syncGroup = SyncGroup.FREQUENT))

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = sync.push()

  override fun pull() = sync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus) = testData.appointment(
      syncStatus = syncStatus,
      facilityUuid = facility.uuid,
      creationFacilityUuid = facility.uuid
  )

  override fun generatePayload() = testData.appointmentPayload(
      facilityUuid = facility.uuid,
      creationFacilityUuid = facility.uuid
  )

  override fun lastPullToken() = lastPullToken

  override fun pushNetworkCall(payloads: List<AppointmentPayload>) = syncApi.push(AppointmentPushRequest(payloads))

  override fun batchSize(): Int = configProvider.blockingGet().batchSize
}
