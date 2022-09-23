package org.simple.clinic.analytics

import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.RumActionType
import org.simple.clinic.platform.analytics.AnalyticsReporter
import org.simple.clinic.platform.analytics.AnalyticsUser
import org.simple.clinic.storage.monitoring.Sampler

class DatadogAnalyticsReporter(
    private val sampler: Sampler
) : AnalyticsReporter {

  override fun setLoggedInUser(user: AnalyticsUser, isANewRegistration: Boolean) {
    // Not reporting users to datadog yet
  }

  override fun resetUser() {
    // Not reporting users to datadog yet
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    if (sampler.sample) {
      GlobalRum.get().addUserAction(
          type = RumActionType.CUSTOM,
          name = event,
          attributes = props,
      )
    }
  }
}
