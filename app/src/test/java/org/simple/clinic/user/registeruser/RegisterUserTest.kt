package org.simple.clinic.user.registeruser

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.registration.RegistrationApi
import org.simple.clinic.registration.RegistrationRequest
import org.simple.clinic.registration.RegistrationResponse
import org.simple.clinic.user.User
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
    val user = PatientMocker.loggedInUser(userUuid)
    val facilityUuid = UUID.fromString("2aa4ccc3-5e4f-4c32-8df3-1304a56ae8b3")
    val facility = PatientMocker.facility(facilityUuid)

    val registrationApi = mock<RegistrationApi>()
    val userDao = mock<User.RoomDao>()
    val facilityRepository = mock<FacilityRepository>()
    val accessTokenPreference = mock<Preference<Optional<String>>>()

    val payload = user.toPayload(facilityUuid)
    whenever(registrationApi.createUser(RegistrationRequest(payload))) doReturn Single.just(RegistrationResponse("accessToken", payload))
    whenever(facilityRepository.associateUserWithFacilities(user, listOf(facilityUuid), facilityUuid)) doReturn Completable.complete()

    val reporter = MockAnalyticsReporter()
    Analytics.addReporter(reporter)

    // when
    val registerUser = RegisterUser(registrationApi, userDao, facilityRepository, accessTokenPreference)

    // when
    registerUser.registerUserAtFacility(user, facility).blockingGet()

    // then
    assertThat(reporter.user).isEqualTo(user)
    assertThat(reporter.isANewRegistration).isTrue()
  }
}
