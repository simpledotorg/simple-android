package org.simple.clinic.crash

import android.app.Application

interface CrashReporter {

  fun init(appContext: Application)

  fun dropBreadcrumb(breadcrumb: Breadcrumb)
}

class NoOpCrashReporter : CrashReporter {

  override fun init(appContext: Application) {}

  override fun dropBreadcrumb(breadcrumb: Breadcrumb) {}
}
