package org.simple.clinic.plumbing.infrastructure

import io.sentry.Sentry
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.user.User
import javax.inject.Inject

class SentryInfrastructure @Inject constructor() : Infrastructure {

  companion object {
    private const val TAG_USER_ID = "userUuid"
    private const val TAG_FACILITY_ID = "facilityUuid"
    private const val TAG_COUNTRY_CODE = "countryCode"
    private const val TAG_DEPLOYMENT = "deployment"
  }

  override fun addDetails(user: User, country: Country, deployment: Deployment) {
    Sentry.setTag(TAG_USER_ID, user.uuid.toString())
    Sentry.setTag(TAG_FACILITY_ID, user.currentFacilityUuid.toString())
    Sentry.setTag(TAG_COUNTRY_CODE, country.isoCountryCode)
    Sentry.setTag(TAG_DEPLOYMENT, deployment.endPoint.toString())
  }

  override fun clear() {
    Sentry.removeTag(TAG_USER_ID)
    Sentry.removeTag(TAG_FACILITY_ID)
    Sentry.removeTag(TAG_COUNTRY_CODE)
    Sentry.removeTag(TAG_DEPLOYMENT)
  }
}
