package org.simple.clinic.plumbing.infrastructure

import android.annotation.SuppressLint
import android.util.Log
import io.reactivex.Observable
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

  @SuppressLint("CheckResult", "LogNotTimber")
  fun track() {
    val currentUser = userSession.loggedInUser()

    currentUser
        .extractIfPresent()
        .subscribeOn(schedulers.io())
        .subscribe { user ->
          val country = appConfigRepository.currentCountry()
          val deployment = appConfigRepository.currentDeployment()

          if (country != null && deployment != null) {
            updateInfrastructures(user, country, deployment)
          }
        }
  }

  private fun updateInfrastructures(
      user: User,
      country: Country,
      deployment: Deployment
  ) {
    infrastructures.forEach { infrastructure -> infrastructure.addDetails(user, country, deployment) }
  }
}
