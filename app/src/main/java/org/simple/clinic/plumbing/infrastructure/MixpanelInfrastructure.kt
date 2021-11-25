package org.simple.clinic.plumbing.infrastructure

import android.app.Application
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.simple.clinic.BuildConfig
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.user.User
import javax.inject.Inject

class MixpanelInfrastructure @Inject constructor(
    private val application: Application
): Infrastructure {

  override fun addDetails(user: User, country: Country, deployment: Deployment) {
    val mixpanel = MixpanelAPI.getInstance(application, BuildConfig.MIXPANEL_TOKEN)

    // We are deliberately not registering the user ID here because Mixpanel has a slightly tricky
    // flow for user identification where we need to track new registrations and logins differently.
    // This is being taken care of elsewhere, in the `Analytics#setLoggedInUser` class.
    mixpanel.registerSuperPropertiesMap(mapOf(
        "facilityId" to user.currentFacilityUuid,
        "countryCode" to country.isoCountryCode,
        "deployment" to deployment.endPoint.toString()
    ))
  }
}
