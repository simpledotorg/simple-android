package org.simple.clinic.plumbing.infrastructure

import android.annotation.SuppressLint
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class UpdateInfrastructureUserDetails @Inject constructor(
    private val infrastructures: List<@JvmSuppressWildcards Infrastructure>,
    private val userSession: UserSession,
    private val appConfigRepository: AppConfigRepository,
    private val schedulers: SchedulersProvider
) {

  @SuppressLint("CheckResult")
  fun track() {
    val currentUser = userSession
        .loggedInUser()
        .extractIfPresent()

    val currentCountry = appConfigRepository
        .currentCountryObservable()
        .extractIfPresent()

    val currentDeployment = appConfigRepository
        .currentDeploymentObservable()
        .extractIfPresent()

    Observables
        .combineLatest(currentUser, currentCountry, currentDeployment)
        .subscribeOn(schedulers.io())
        .take(1)
        .subscribe { (user, country, deployment) -> updateInfrastructures(user, country, deployment) }
  }

  private fun updateInfrastructures(
      user: User,
      country: Country,
      deployment: Deployment
  ) {
    infrastructures.forEach { infrastructure -> infrastructure.addDetails(user, country, deployment) }
  }
}
