package org.simple.clinic.overdue.communication

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.AuthenticationRule
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
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

  @get:Rule
  val authenticationRule = AuthenticationRule()

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_creating_new_communication_then_the_communication_should_be_saved() {
    val appointmentId = UUID.randomUUID()

    repository
        .create(appointmentId, Communication.Type.MANUAL_CALL, result = Communication.Result.UNAVAILABLE)
        .blockingAwait()

    val savedCommunication = repository.recordsWithSyncStatus(SyncStatus.PENDING).blockingGet()[0]
    savedCommunication.apply {
      assertThat(this.appointmentUuid).isEqualTo(appointmentId)
      assertThat(this.type).isEqualTo(Communication.Type.MANUAL_CALL)
      assertThat(this.result).isEqualTo(Communication.Result.UNAVAILABLE)
      assertThat(this.syncStatus).isEqualTo(SyncStatus.PENDING)
    }
  }
}
