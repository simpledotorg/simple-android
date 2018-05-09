package org.resolvetosavelives.red

import android.app.Application
import timber.log.Timber

class RedApp : Application() {

  override fun onCreate() {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
  }
}
