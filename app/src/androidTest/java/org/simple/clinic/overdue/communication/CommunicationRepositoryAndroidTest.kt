package org.simple.clinic.overdue.communication

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
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class CommunicationRepositoryAndroidTest {

  @Inject
  lateinit var repository: CommunicationRepository

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
  fun when_creating_new_communication_then_the_communication_should_be_saved() {
    val appointmentId = UUID.randomUUID()

    repository
        .create(appointmentId, Communication.Type.MANUAL_CALL, result = Communication.Result.AGREED_TO_VISIT)
        .blockingAwait()

    val savedCommunication = repository.pendingSyncRecords().blockingGet()[0]
    savedCommunication.apply {
      assertThat(this.appointmentUuid).isEqualTo(appointmentId)
      assertThat(this.type).isEqualTo(Communication.Type.MANUAL_CALL)
      assertThat(this.result).isEqualTo(Communication.Result.AGREED_TO_VISIT)
      assertThat(this.syncStatus).isEqualTo(SyncStatus.PENDING)
    }
  }

  @After
  fun tearDown() {
    userSession.logout().blockingAwait()
  }
}
