package org.simple.clinic.analytics

import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.platform.analytics.AnalyticsReporter
import org.simple.clinic.platform.analytics.AnalyticsUser
import org.simple.clinic.storage.monitoring.Sampler

class MixpanelAnalyticsReporter(
    app: ClinicApp,
    private val sampler: Sampler
) : AnalyticsReporter {

  private val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(app, BuildConfig.MIXPANEL_TOKEN, true)

  override fun setLoggedInUser(user: AnalyticsUser, isANewRegistration: Boolean) {
    synchronized(mixpanel) {
      val userId = user.id.toString()

      if (isANewRegistration) {
        mixpanel.alias(userId, null)
      }

      mixpanel.identify(userId)

      // The current facility, deployment, etc are being tracked in `MixpanelInfrastructure`
      with(mixpanel.people) {
        identify(userId)
        set("id", userId)
      }
    }
  }

  override fun resetUser() {
    synchronized(mixpanel) {
      mixpanel.reset()
    }
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    if (sampler.sample) {
      synchronized(mixpanel) {
        mixpanel.trackMap(event, props)
      }
    }
  }
}
