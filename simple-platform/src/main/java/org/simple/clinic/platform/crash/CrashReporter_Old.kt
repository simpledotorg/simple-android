package org.simple.clinic.platform.crash

import android.app.Application

interface CrashReporter_Old {

  fun init(appContext: Application)

  fun dropBreadcrumb(breadcrumb: Breadcrumb)

  fun report(e: Throwable)
}

class NoOpCrashReporter : CrashReporter_Old {

  override fun report(e: Throwable) {}

  override fun init(appContext: Application) {}

  override fun dropBreadcrumb(breadcrumb: Breadcrumb) {}
}
