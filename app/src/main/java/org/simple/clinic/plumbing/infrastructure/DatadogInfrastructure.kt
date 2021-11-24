package org.simple.clinic.plumbing.infrastructure

import com.datadog.android.Datadog
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.user.User
import javax.inject.Inject

class DatadogInfrastructure @Inject constructor(): Infrastructure {

  override fun addDetails(user: User, country: Country, deployment: Deployment) {
    Datadog.setUserInfo(
        id = user.uuid.toString(),
        name = user.fullName,
        extraInfo = mapOf(
            "facilityId" to user.currentFacilityUuid.toString(),
            "countryCode" to country.isoCountryCode,
            "deployment" to deployment.endPoint.toString()
        )
    )
  }
}
