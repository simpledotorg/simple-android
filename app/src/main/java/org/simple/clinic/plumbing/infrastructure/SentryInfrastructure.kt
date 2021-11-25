package org.simple.clinic.plumbing.infrastructure

import io.sentry.Sentry
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.user.User
import javax.inject.Inject

class SentryInfrastructure @Inject constructor(): Infrastructure {

  override fun addDetails(user: User, country: Country, deployment: Deployment) {
    Sentry.setTag("userUuid", user.uuid.toString())
    Sentry.setTag("facilityUuid", user.currentFacilityUuid.toString())
    Sentry.setTag("countryCode", country.isoCountryCode)
    Sentry.setTag("deployment", deployment.endPoint.toString())
  }
}
