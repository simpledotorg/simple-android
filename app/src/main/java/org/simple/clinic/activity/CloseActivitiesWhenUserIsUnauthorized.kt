package org.simple.clinic.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterTrue
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.lang.ref.WeakReference
import javax.inject.Inject

class CloseActivitiesWhenUserIsUnauthorized @Inject constructor(
    private val userSession: UserSession,
    private val schedulersProvider: SchedulersProvider
) : SimpleActivityLifecycleCallbacks() {

  private val activitiesToFinish = mutableListOf<WeakReference<Activity>>()

  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    super.onActivityCreated(activity, savedInstanceState)
    if (activity !is TheActivity) {
      activitiesToFinish.add(WeakReference(activity))
    }
  }

  override fun onActivityDestroyed(activity: Activity) {
    super.onActivityDestroyed(activity)
    if (activity !is TheActivity) {
      activitiesToFinish.removeAll { ref -> activity === ref.get() }
    }
  }

  @SuppressLint("CheckResult")
  fun listen() {
    userSession
        .isUserUnauthorized()
        .subscribeOn(schedulersProvider.io())
        .filterTrue()
        .observeOn(schedulersProvider.ui())
        .subscribe { finishAllActivities() }
  }

  private fun finishAllActivities() {
    activitiesToFinish
        .toList()
        .mapNotNull { activityRef -> activityRef.get() }
        .forEach { it.finish() }
  }
}
