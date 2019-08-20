package org.simple.clinic.analytics

import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp

class MixpanelAnalyticsReporter(app: ClinicApp) : AnalyticsReporter {

  private val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(app, BuildConfig.MIXPANEL_TOKEN)

  override fun setUserIdentity(id: String) {
    synchronized(mixpanel) {
      mixpanel.identify(id)
      mixpanel.people.identify(id)
    }
  }

  override fun resetUserIdentity() {
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
