package org.simple.clinic.summary.addphone

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class MissingPhoneReminderRepositoryAndroidTest {

  @Inject
  lateinit var repository: MissingPhoneReminderRepository

  @Inject
  lateinit var dao: MissingPhoneReminder.RoomDao

  @Inject
  lateinit var clock: UtcClock

  private val patientUuid = UUID.randomUUID()

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun saving_a_reminder_should_work() {
    val remindedAt = Instant.now(clock)
    repository.markReminderAsShownFor(patientUuid).blockingAwait()

    val savedReminder = dao.get(patientUuid).blockingFirst().first()
    assertThat(savedReminder).isEqualTo(MissingPhoneReminder(patientUuid, remindedAt))
  }

  @Test
  fun when_a_reminder_is_saved_then_retrieving_it_should_work() {
    val nonExistentPatientUuid = UUID.randomUUID()
    val hasASavedReminder = repository.hasShownReminderFor(nonExistentPatientUuid).blockingGet()
    assertThat(hasASavedReminder).isFalse()
  }

  @Test
  fun when_a_reminder_isnt_present_then_retrieving_it_shouldnt_work() {
    repository.markReminderAsShownFor(patientUuid).blockingAwait()

    val hasASavedReminder = repository.hasShownReminderFor(patientUuid).blockingGet()
    assertThat(hasASavedReminder).isTrue()
  }
}
