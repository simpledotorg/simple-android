package org.simple.clinic.user.registeruser

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.login.UsersApi
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.AnalyticsUser
import org.simple.clinic.registration.RegistrationRequest
import org.simple.clinic.registration.RegistrationResponse
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toPayload
import java.util.UUID

class RegisterUserTest {

  @After
  fun tearDown() {
    Analytics.clearReporters()
  }

  @Test
  fun `when user registers, set the registered user in analytics`() {
    // given
    val userUuid = UUID.fromString("0e7b2b09-dbb0-4de6-b66d-6afe834c14ed")
    val facilityUuid = UUID.fromString("2aa4ccc3-5e4f-4c32-8df3-1304a56ae8b3")
    val facility = TestData.facility(facilityUuid)
    val user = TestData.loggedInUser(userUuid, registrationFacilityUuid = facilityUuid, currentFacilityUuid = facilityUuid)

    val usersApi = mock<UsersApi>()
    val userDao = mock<User.RoomDao>()
    val facilityRepository = mock<FacilityRepository>()
    val accessTokenPreference = mock<Preference<Optional<String>>>()

    val payload = user.toPayload(facilityUuid)
    val savedUser = user.copy(loggedInStatus = LOGGED_IN)
    whenever(usersApi.createUser(RegistrationRequest(payload))) doReturn Single.just(RegistrationResponse("accessToken", payload))
    whenever(facilityRepository.setCurrentFacility(facilityUuid)) doReturn Completable.complete()

    val reporter = MockAnalyticsReporter()
    Analytics.addReporter(reporter)

    // when
    val registerUser = RegisterUser(usersApi, userDao, facilityRepository, accessTokenPreference)

    // when
    registerUser.registerUserAtFacility(user, facility).blockingGet()

    // then
    assertThat(reporter.user).isEqualTo(AnalyticsUser(savedUser.uuid, savedUser.fullName))
    assertThat(reporter.isANewRegistration).isTrue()
  }
}
