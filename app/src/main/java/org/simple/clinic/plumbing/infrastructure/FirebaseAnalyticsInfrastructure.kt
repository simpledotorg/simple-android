package org.simple.clinic.plumbing.infrastructure

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.user.User
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class FirebaseAnalyticsInfrastructure @Inject constructor(
    private val application: Application
) : Infrastructure {

  private val firebaseAnalytics by unsafeLazy {
    FirebaseAnalytics.getInstance(application)
  }

  override fun addDetails(user: User, country: Country, deployment: Deployment) {
    firebaseAnalytics.setUserProperty("facilityId", user.currentFacilityUuid.toString())
    firebaseAnalytics.setUserProperty("countryCode", country.isoCountryCode)
    firebaseAnalytics.setUserProperty("deployment", deployment.endPoint.toString())
  }

  override fun clear() {
    firebaseAnalytics.resetAnalyticsData()
  }
}
