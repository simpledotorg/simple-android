package org.simple.clinic.user

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.util.Rules
import java.util.UUID
import javax.inject.Inject


class UserDaoAndroidTest {

  @Inject
  lateinit var sharedPrefs: SharedPreferences

  @Inject
  lateinit var appDatabase: AppDatabase

  @get:Rule
  val ruleChain: RuleChain = Rules.global()

  private val facility = TestData.facility(uuid = UUID.fromString("2fdb95f0-f0e4-42e4-b504-2bb09f42d7dd"))

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    appDatabase.facilityDao().save(listOf(facility))
  }

  @After
  fun tearDown() {
    appDatabase.clearAllTables()
  }

  /**
   * This was added after we found that Room doesn't complain if incorrect values
   * are passed for @Insert's onConflict strategy and [User.RoomDao.createOrUpdate]
   * was ignoring updates.
   */
  @Test
  fun update_should_work_correctly() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("88965136-1de2-4e2c-9ea9-36a170820286"),
        status = UserStatus.WaitingForApproval,
        registrationFacilityUuid = facility.uuid
    )
    val updatedUser = user.copy(status = UserStatus.ApprovedForSyncing)

    appDatabase.userDao().createOrUpdate(user)
    appDatabase.userDao().createOrUpdate(updatedUser)

    val updatedUserInDatabase = appDatabase.userDao().userImmediate()
    assertThat(updatedUserInDatabase).isEqualTo(updatedUser)
  }
}
