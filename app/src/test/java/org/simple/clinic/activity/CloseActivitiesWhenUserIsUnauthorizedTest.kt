package org.simple.clinic.activity

import android.app.Activity
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class CloseActivitiesWhenUserIsUnauthorizedTest {

  private val userUnauthorizedSubject = PublishSubject.create<Boolean>()
  private val userSession = mock<UserSession>()

  private val closeActivitiesWhenUserIsUnauthorized = CloseActivitiesWhenUserIsUnauthorized(userSession, TrampolineSchedulersProvider())

  @Before
  fun setUp() {
    whenever(userSession.isUserUnauthorized())
        .thenReturn(userUnauthorizedSubject)

    closeActivitiesWhenUserIsUnauthorized.listen()
  }

  @Test
  fun `any created activity that is not TheActivity must be finished if the user gets unauthorized`() {
    // given
    val aCreatedTheActivity = mock<TheActivity>()
    val aCreatedActivity = mock<Activity>()
    simulateActivityCreated(aCreatedActivity)
    simulateActivityCreated(aCreatedTheActivity)

    // then
    userUnauthorizedSubject.onNext(false)
    verify(aCreatedTheActivity, never()).finish()
    verify(aCreatedActivity, never()).finish()
    reset(aCreatedTheActivity, aCreatedActivity)

    userUnauthorizedSubject.onNext(true)
    verify(aCreatedTheActivity, never()).finish()
    verify(aCreatedActivity).finish()
    reset(aCreatedTheActivity, aCreatedActivity)

    // This block is intentionally repeated to ensure that the user session unauthorized stream
    // is observed continuously and not just for the one emission.

    userUnauthorizedSubject.onNext(true)
    verify(aCreatedTheActivity, never()).finish()
    verify(aCreatedActivity).finish()
  }

  @Test
  fun `any started activity that is not TheActivity must be finished if the user gets unauthorized`() {
    // given
    val aStartedTheActivity = mock<TheActivity>()
    val aStartedActivity = mock<Activity>()

    simulateActivityStarted(aStartedActivity)
    simulateActivityStarted(aStartedTheActivity)

    // then
    userUnauthorizedSubject.onNext(false)
    verify(aStartedTheActivity, never()).finish()
    verify(aStartedActivity, never()).finish()
    reset(aStartedTheActivity, aStartedActivity)

    userUnauthorizedSubject.onNext(true)
    verify(aStartedTheActivity, never()).finish()
    verify(aStartedActivity).finish()
    reset(aStartedTheActivity, aStartedActivity)

    // This block is intentionally repeated to ensure that the user session unauthorized stream
    // is observed continuously and not just for the one emission.

    userUnauthorizedSubject.onNext(true)
    verify(aStartedTheActivity, never()).finish()
    verify(aStartedActivity).finish()
  }

  @Test
  fun `any resumed activity that is not TheActivity must be finished if the user gets unauthorized`() {
    // given
    val aResumedTheActivity = mock<TheActivity>()
    val aResumedActivity = mock<Activity>()

    simulateActivityResumed(aResumedActivity)
    simulateActivityResumed(aResumedTheActivity)

    // then
    userUnauthorizedSubject.onNext(false)
    verify(aResumedTheActivity, never()).finish()
    verify(aResumedActivity, never()).finish()
    reset(aResumedTheActivity, aResumedActivity)

    userUnauthorizedSubject.onNext(true)
    verify(aResumedTheActivity, never()).finish()
    verify(aResumedActivity).finish()
    reset(aResumedTheActivity, aResumedActivity)

    // This block is intentionally repeated to ensure that the user session unauthorized stream
    // is observed continuously and not just for the one emission.

    userUnauthorizedSubject.onNext(true)
    verify(aResumedTheActivity, never()).finish()
    verify(aResumedActivity).finish()
  }

  @Test
  fun `any paused activity that is not TheActivity must be finished if the user gets unauthorized`() {
    // given
    val aPausedTheActivity = mock<TheActivity>()
    val aPausedActivity = mock<Activity>()

    simulateActivityPaused(aPausedActivity)
    simulateActivityPaused(aPausedTheActivity)

    // then
    userUnauthorizedSubject.onNext(false)
    verify(aPausedTheActivity, never()).finish()
    verify(aPausedActivity, never()).finish()
    reset(aPausedTheActivity, aPausedActivity)

    userUnauthorizedSubject.onNext(true)
    verify(aPausedTheActivity, never()).finish()
    verify(aPausedActivity).finish()
    reset(aPausedTheActivity, aPausedActivity)

    // This block is intentionally repeated to ensure that the user session unauthorized stream
    // is observed continuously and not just for the one emission.
    userUnauthorizedSubject.onNext(true)
    verify(aPausedTheActivity, never()).finish()
    verify(aPausedActivity).finish()
  }

  @Test
  fun `any stopped activity that is not TheActivity must be finished if the user gets unauthorized`() {
    // given
    val aStoppedTheActivity = mock<TheActivity>()
    val aStoppedActivity = mock<Activity>()

    simulateActivityStopped(aStoppedActivity)
    simulateActivityStopped(aStoppedTheActivity)

    // then
    userUnauthorizedSubject.onNext(false)
    verify(aStoppedTheActivity, never()).finish()
    verify(aStoppedActivity, never()).finish()
    reset(aStoppedTheActivity, aStoppedActivity)

    userUnauthorizedSubject.onNext(true)
    verify(aStoppedTheActivity, never()).finish()
    verify(aStoppedActivity).finish()
    reset(aStoppedTheActivity, aStoppedActivity)

    // This block is intentionally repeated to ensure that the user session unauthorized stream
    // is observed continuously and not just for the one emission.

    userUnauthorizedSubject.onNext(true)
    verify(aStoppedTheActivity, never()).finish()
    verify(aStoppedActivity).finish()
  }

  @Test
  fun `any destroyed activity must not be finished if the user gets unauthorized`() {
    // given
    val aDestroyedTheActivity = mock<TheActivity>()
    val aDestroyedActivity = mock<Activity>()

    simulateActivityDestroyed(aDestroyedActivity)
    simulateActivityDestroyed(aDestroyedTheActivity)

    // then
    userUnauthorizedSubject.onNext(false)
    verify(aDestroyedTheActivity, never()).finish()
    verify(aDestroyedActivity, never()).finish()
    reset(aDestroyedTheActivity, aDestroyedActivity)

    userUnauthorizedSubject.onNext(true)
    verify(aDestroyedTheActivity, never()).finish()
    verify(aDestroyedActivity, never()).finish()
    reset(aDestroyedTheActivity, aDestroyedActivity)

    // This block is intentionally repeated to ensure that the user session unauthorized stream
    // is observed continuously and not just for the one emission.

    userUnauthorizedSubject.onNext(true)
    verify(aDestroyedTheActivity, never()).finish()
    verify(aDestroyedActivity, never()).finish()
  }

  private fun simulateActivityCreated(activity: Activity) {
    closeActivitiesWhenUserIsUnauthorized.onActivityCreated(activity, null)
  }

  private fun simulateActivityStarted(activity: Activity) {
    closeActivitiesWhenUserIsUnauthorized.onActivityCreated(activity, null)
    closeActivitiesWhenUserIsUnauthorized.onActivityStarted(activity)
  }

  private fun simulateActivityResumed(activity: Activity) {
    closeActivitiesWhenUserIsUnauthorized.onActivityCreated(activity, null)
    closeActivitiesWhenUserIsUnauthorized.onActivityStarted(activity)
    closeActivitiesWhenUserIsUnauthorized.onActivityResumed(activity)
  }

  private fun simulateActivityPaused(activity: Activity) {
    closeActivitiesWhenUserIsUnauthorized.onActivityCreated(activity, null)
    closeActivitiesWhenUserIsUnauthorized.onActivityStarted(activity)
    closeActivitiesWhenUserIsUnauthorized.onActivityResumed(activity)
    closeActivitiesWhenUserIsUnauthorized.onActivityPaused(activity)
  }

  private fun simulateActivityStopped(activity: Activity) {
    closeActivitiesWhenUserIsUnauthorized.onActivityCreated(activity, null)
    closeActivitiesWhenUserIsUnauthorized.onActivityStarted(activity)
    closeActivitiesWhenUserIsUnauthorized.onActivityResumed(activity)
    closeActivitiesWhenUserIsUnauthorized.onActivityPaused(activity)
    closeActivitiesWhenUserIsUnauthorized.onActivityStopped(activity)
  }

  private fun simulateActivityDestroyed(activity: Activity) {
    closeActivitiesWhenUserIsUnauthorized.onActivityCreated(activity, null)
    closeActivitiesWhenUserIsUnauthorized.onActivityStarted(activity)
    closeActivitiesWhenUserIsUnauthorized.onActivityResumed(activity)
    closeActivitiesWhenUserIsUnauthorized.onActivityPaused(activity)
    closeActivitiesWhenUserIsUnauthorized.onActivityStopped(activity)
    closeActivitiesWhenUserIsUnauthorized.onActivityDestroyed(activity)
  }
}
