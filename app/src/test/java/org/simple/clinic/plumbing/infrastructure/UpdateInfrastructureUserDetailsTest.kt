package org.simple.clinic.plumbing.infrastructure

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.simple.clinic.TestData
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
    val countrySubject = PublishSubject.create<Optional<Country>>()
    val deploymentSubject = PublishSubject.create<Optional<Deployment>>()
    val appConfigRepository = mock<AppConfigRepository>()
    whenever(appConfigRepository.currentCountryObservable()).thenReturn(countrySubject)
    whenever(appConfigRepository.currentDeploymentObservable()).thenReturn(deploymentSubject)

    val firstInfrastructure = mock<Infrastructure>()
    val secondInfrastructure = mock<Infrastructure>()
    val updateInfrastructureUserDetails = UpdateInfrastructureUserDetails(
        infrastructures = listOf(firstInfrastructure, secondInfrastructure),
        userSession = userSession,
        appConfigRepository = appConfigRepository,
        schedulers = TestSchedulersProvider.trampoline()
    )

    // when
    updateInfrastructureUserDetails.track()

    // then
    verifyZeroInteractions(firstInfrastructure, secondInfrastructure)

    // when
    userSubject.onNext(Optional.of(user))

    // then
    verifyZeroInteractions(firstInfrastructure, secondInfrastructure)

    // when
    countrySubject.onNext(Optional.of(country))

    // then
    verifyZeroInteractions(firstInfrastructure, secondInfrastructure)

    // when
    deploymentSubject.onNext(Optional.of(deployment))

    // then
    verify(firstInfrastructure).addDetails(user, country, deployment)
    verify(secondInfrastructure).addDetails(user, country, deployment)
    verifyNoMoreInteractions(firstInfrastructure, secondInfrastructure)
  }
}
