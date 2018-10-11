package org.simple.clinic.crash

import android.app.Application

interface CrashReporter {

  fun init(appContext: Application)
}
