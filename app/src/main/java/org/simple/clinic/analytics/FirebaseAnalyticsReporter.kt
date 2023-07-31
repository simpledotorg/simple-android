package org.simple.clinic.analytics

import android.app.Application
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import org.simple.clinic.platform.analytics.AnalyticsReporter
import org.simple.clinic.platform.analytics.AnalyticsUser
import org.simple.clinic.util.unsafeLazy

class FirebaseAnalyticsReporter(
    private val application: Application
) : AnalyticsReporter {

  private val firebaseAnalytics by unsafeLazy {
    FirebaseAnalytics.getInstance(application)
  }

  override fun setLoggedInUser(user: AnalyticsUser, isANewRegistration: Boolean) {
    synchronized(firebaseAnalytics) {
      val userId = user.id.toString()

      // The current facility, deployment, etc are being tracked in `FirebaseAnalyticsInfrastructure`
      firebaseAnalytics.setUserId(userId)
      firebaseAnalytics.setUserProperty("is_new_registration", isANewRegistration.toString())
    }
  }

  override fun resetUser() {
    synchronized(firebaseAnalytics) {
      firebaseAnalytics.resetAnalyticsData()
    }
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    synchronized(firebaseAnalytics) {
      val propsBundle = bundleOf(*props.toList().toTypedArray())
      firebaseAnalytics.logEvent(event, propsBundle)
    }
  }
}
