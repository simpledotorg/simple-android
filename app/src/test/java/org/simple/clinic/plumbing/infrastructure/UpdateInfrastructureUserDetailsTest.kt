package org.simple.clinic.plumbing.infrastructure

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.Optional
import java.util.UUID

class UpdateInfrastructureUserDetailsTest {

  @Test
  fun `the user details must be set on each infrastructure registered`() {
    // given
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("3daad1ea-ef5d-41fc-9b42-a1bb82c80bd7"),
        registrationFacilityUuid = UUID.fromString("10fe996b-373a-4c42-998e-98ed84f0a7e3")
    )
    val userSubject = PublishSubject.create<Optional<User>>()
    val userSession = mock<UserSession>()
    whenever(userSession.loggedInUser()).thenReturn(userSubject)

    val country = TestData.country()
    val deployment = country.deployments.first()
    val appConfigRepository = mock<AppConfigRepository>()

    val firstInfrastructure = mock<Infrastructure>()
    val secondInfrastructure = mock<Infrastructure>()
    val updateInfrastructureUserDetails = UpdateInfrastructureUserDetails(
        infrastructures = listOf(firstInfrastructure, secondInfrastructure),
        userSession = userSession,
        appConfigRepository = appConfigRepository,
        schedulers = TestSchedulersProvider.trampoline()
    )

    whenever(appConfigRepository.currentCountry()).thenReturn(country)
    whenever(appConfigRepository.currentDeployment()).thenReturn(deployment)

    // when
    updateInfrastructureUserDetails.track()

    // then
    verifyNoInteractions(firstInfrastructure, secondInfrastructure)

    // when
    userSubject.onNext(Optional.of(user))

    // then
    verify(firstInfrastructure).addDetails(user, country, deployment)
    verify(secondInfrastructure).addDetails(user, country, deployment)
    verifyNoMoreInteractions(firstInfrastructure, secondInfrastructure)
  }
}
