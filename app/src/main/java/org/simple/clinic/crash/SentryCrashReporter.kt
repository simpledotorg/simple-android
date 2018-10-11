package org.simple.clinic.crash

import android.app.Application
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory

class SentryCrashReporter : CrashReporter {

  override fun init(appContext: Application) {
    Sentry.init(AndroidSentryClientFactory(appContext))
  }
}
