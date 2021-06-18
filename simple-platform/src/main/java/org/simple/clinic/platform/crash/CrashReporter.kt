package org.simple.clinic.platform.crash

import android.app.Application

object CrashReporter: CrashReporter_Old {

  override fun init(appContext: Application) {
  }

  override fun dropBreadcrumb(breadcrumb: Breadcrumb) {
  }

  override fun report(e: Throwable) {
  }
}
