package org.simple.clinic.benchmark

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.user.User
import javax.inject.Inject
import javax.inject.Provider

class CanaryBenchmarkTest : BaseBenchmarkTest() {

  @Inject
  lateinit var userDaoProvider: Provider<User.RoomDao>

  init {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun benchmark_test_timings_should_be_tracked() {
    Thread.sleep(1500L)
  }

  @Test
  fun databases_should_be_saved_and_restored() {
    val userDao = userDaoProvider.get()
    val currentUser = userDao.userImmediate()!!
    assertThat(currentUser.fullName).isEqualTo("Android Test User")
    val modifiedUser = currentUser.copy(fullName = "Modified User")
    userDao.createOrUpdate(modifiedUser)
    assertThat(userDao.userImmediate()!!.fullName).isEqualTo("Modified User")
  }
}
