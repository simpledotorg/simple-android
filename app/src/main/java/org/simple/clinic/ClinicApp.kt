package org.simple.clinic

import android.support.multidex.MultiDexApplication
import com.gabrielittner.threetenbp.LazyThreeTen
import io.reactivex.schedulers.Schedulers
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.di.AppComponent
import org.simple.clinic.user.User
import org.simple.clinic.util.Just
import timber.log.Timber

abstract class ClinicApp : MultiDexApplication() {

  companion object {
    lateinit var appComponent: AppComponent
  }

  override fun onCreate() {
    super.onCreate()

    @Suppress("ConstantConditionIf")
    if (BuildConfig.API_ENDPOINT == "null") {
      throw AssertionError("API endpoint cannot be null!")
    }

    LazyThreeTen.init(this)

    appComponent = buildDaggerGraph()

    Sentry.init(AndroidSentryClientFactory(applicationContext))
  }

  abstract fun buildDaggerGraph(): AppComponent

  protected fun keepUserIdUpdatedInAnalytics() {
    appComponent.userSession()
        .loggedInUser()
        .subscribeOn(Schedulers.io())
        .filter { it is Just<User> }
        .map { (user) -> user!! }
        .filter { it.loggedInStatus == User.LoggedInStatus.LOGGED_IN }
        .subscribe({
          Analytics.setUserId(it.uuid)
        }, {
          Timber.e(it, "Could not update user ID in analytics")
        })
  }
}
