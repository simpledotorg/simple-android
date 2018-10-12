package org.simple.clinic.crash

import android.app.Application

interface CrashReporter {

  fun init(appContext: Application)

  fun dropBreadcrumb(breadcrumb: Breadcrumb)

  fun report(e: Throwable)
}

class NoOpCrashReporter : CrashReporter {

  override fun report(e: Throwable) {}

  override fun init(appContext: Application) {}

  override fun dropBreadcrumb(breadcrumb: Breadcrumb) {}
}
