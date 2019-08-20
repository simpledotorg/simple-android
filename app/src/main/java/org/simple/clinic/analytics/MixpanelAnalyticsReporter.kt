package org.simple.clinic.analytics

import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.user.User

class MixpanelAnalyticsReporter(app: ClinicApp) : AnalyticsReporter {

  private val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(app, BuildConfig.MIXPANEL_TOKEN)

  override fun setLoggedInUser(user: User) {
    synchronized(mixpanel) {
      val userId = user.uuid.toString()

      mixpanel.identify(userId)
      mixpanel.people.identify(userId)
    }
  }

  override fun resetUser() {
    synchronized(mixpanel) {
      mixpanel.reset()
    }
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    synchronized(mixpanel) {
      mixpanel.trackMap(event, props)
    }
  }
}
