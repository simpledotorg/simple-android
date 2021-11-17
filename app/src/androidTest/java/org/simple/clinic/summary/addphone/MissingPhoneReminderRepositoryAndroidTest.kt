package org.simple.clinic.summary.addphone

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.util.Rules
import org.simple.clinic.util.UtcClock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class MissingPhoneReminderRepositoryAndroidTest {

  @Inject
  lateinit var repository: MissingPhoneReminderRepository

  @Inject
  lateinit var dao: MissingPhoneReminder.RoomDao

  @Inject
  lateinit var clock: UtcClock

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(SaveDatabaseRule())

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

  @Suppress("LocalVariableName", "IllegalIdentifier")
  @Test
  fun when_a_missing_phone_reminder_is_present_for_a_patient_then_checking_whether_a_reminder_has_been_shown_should_return_true() {
    // given
    val `patient who has been shown reminder` = UUID.fromString("b9eac296-9b12-4601-8462-e07b5338acd3")

    repository.markReminderAsShownFor(`patient who has been shown reminder`).blockingAwait()

    // when
    val hasASavedReminder = repository.hasShownReminderForPatient(`patient who has been shown reminder`)

    // then
    assertThat(hasASavedReminder).isTrue()
  }

  @Suppress("LocalVariableName", "IllegalIdentifier")
  @Test
  fun when_a_missing_phone_reminder_is_not_present_for_a_patient_then_checking_whether_a_reminder_has_been_shown_should_return_false() {
    //given
    val `patient who has been shown reminder` = UUID.fromString("b9eac296-9b12-4601-8462-e07b5338acd3")
    val `patient who has not been shown reminder` = UUID.fromString("d0414d6d-9813-4d85-ad61-634f7cddab76")

    repository.markReminderAsShownFor(`patient who has been shown reminder`).blockingAwait()

    // when
    val hasASavedReminder = repository.hasShownReminderForPatient(`patient who has not been shown reminder`)

    // then
    assertThat(hasASavedReminder).isFalse()
  }
}
