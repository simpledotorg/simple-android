package org.simple.clinic.overdue

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.login.LoginResult
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.user.UserSession
import org.threeten.bp.LocalDate
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class AppointmentRepositoryAndroidTest {

  @Inject
  lateinit var repository: AppointmentRepository

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)

    val loginResult = userSession.saveOngoingLoginEntry(testData.qaOngoingLoginEntry())
        .andThen(userSession.loginWithOtp(testData.qaUserOtp()))
        .blockingGet()
    assertThat(loginResult).isInstanceOf(LoginResult.Success::class.java)
  }

  @Test
  fun when_creating_new_appointment_then_the_appointment_should_be_saved() {
    val patientId = UUID.randomUUID()
    val appointmentDate = LocalDate.now()
    repository.schedule(patientId, appointmentDate).blockingAwait()

    val savedAppointment = repository.pendingSyncRecords().blockingGet().first()
    savedAppointment.apply {
      assertThat(this.patientUuid).isEqualTo(patientId)
      assertThat(this.date).isEqualTo(appointmentDate)
      assertThat(this.date).isEqualTo(appointmentDate)
      assertThat(this.status).isEqualTo(Appointment.Status.SCHEDULED)
      assertThat(this.statusReason).isEqualTo(Appointment.StatusReason.NOT_CALLED_YET)
      assertThat(this.syncStatus).isEqualTo(SyncStatus.PENDING)
    }
  }

  @After
  fun tearDown() {
    userSession.logout().blockingAwait()
  }
}
