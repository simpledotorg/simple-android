package org.simple.clinic.overdue

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
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class AppointmentSyncAndroidTest : BaseSyncCoordinatorAndroidTest<Appointment, AppointmentPayload>() {

  @Inject
  lateinit var repository: AppointmentRepository

  @Inject
  @field:Named("last_appointment_pull_token")
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var sync: AppointmentSync

  @Inject
  lateinit var syncApi: AppointmentSyncApiV1

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var appointmentConfigProvider: Single<AppointmentConfig>

  val config: AppointmentConfig
    get() = appointmentConfigProvider.blockingGet()

  @get:Rule
  val authenticationRule = AuthenticationRule()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  override fun push() = sync.push()

  override fun pull() = sync.pull()

  override fun repository() = repository

  override fun generateRecord(syncStatus: SyncStatus) = testData.appointment(syncStatus)

  override fun generatePayload() = testData.appointmentPayload()

  override fun lastPullToken(): Preference<Optional<String>> = lastPullToken

  override fun pushNetworkCall(payloads: List<AppointmentPayload>): Single<DataPushResponse> {
    val request = AppointmentPushRequest(payloads)
    return syncApi.push(request)
  }
}
