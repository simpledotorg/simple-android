package org.simple.clinic.user

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.util.Rules
import java.util.UUID
import javax.inject.Inject


class OngoingLoginEntryRepositoryTest {

  @Inject
  lateinit var repository: OngoingLoginEntryRepository

  @Inject
  lateinit var database: AppDatabase

  private val expectedException = ExpectedException.none()

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(expectedException)

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_saving_ongoing_entry_it_should_get_saved() {
    val userId = UUID.randomUUID()
    val phone = "998877665"
    val pin = "9090"

    repository.saveLoginEntry(OngoingLoginEntry(uuid = userId, phoneNumber = phone, pin = pin, capabilities = null)).blockingAwait()

    val savedEntry = repository.entry().blockingGet()
    savedEntry.apply {
      assertThat(this.uuid).isEqualTo(userId)
      assertThat(this.phoneNumber).isEqualTo(phone)
      assertThat(this.pin).isEqualTo(pin)
    }
  }
}
