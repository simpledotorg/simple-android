package org.simple.clinic.user

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class OngoingLoginEntryRepositoryTest {

  @Inject
  lateinit var repository: OngoingLoginEntryRepository

  @Inject
  lateinit var database: AppDatabase

  @Rule
  @JvmField
  val expectedException: ExpectedException = ExpectedException.none()

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_saving_ongoing_entry_it_should_get_saved() {
    val userId = UUID.randomUUID()
    val phone = "998877665"
    val pin = "9090"

    repository.saveLoginEntry(OngoingLoginEntry(uuid = userId, phoneNumber = phone, pin = pin)).blockingAwait()

    val savedEntry = repository.entry().blockingGet()
    savedEntry.apply {
      assertThat(this.uuid).isEqualTo(userId)
      assertThat(this.phoneNumber).isEqualTo(phone)
      assertThat(this.pin).isEqualTo(pin)
    }
  }

  @Test
  fun when_clearing_entry_it_should_get_deleted_from_db() {
    val userId = UUID.randomUUID()
    val phone = "998877665"
    val pin = "9090"

    repository.saveLoginEntry(OngoingLoginEntry(uuid = userId, phoneNumber = phone, pin = pin)).blockingAwait()

    val savedEntry = repository.entry().blockingGet()
    savedEntry.apply {
      assertThat(this.uuid).isEqualTo(userId)
    }

    repository.clearLoginEntry().blockingAwait()

    expectedException.expect(IllegalStateException::class.java)
    expectedException.expectMessage("User not present")

    repository.entry().blockingGet()
  }
}
