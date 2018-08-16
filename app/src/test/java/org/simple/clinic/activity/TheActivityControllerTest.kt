package org.simple.clinic.activity

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit

class TheActivityControllerTest {

  private val lockInMinutes = 15L

  private val activity = mock<TheActivity>()
  private val userSession = mock<UserSession>()
  private val lockAfterTimestamp = mock<Preference<Instant>>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  lateinit var controller: TheActivityController

  @Before
  fun setUp() {
    val appLockConfig = AppLockConfig(lockAfterTimeMillis = TimeUnit.MINUTES.toMillis(lockInMinutes))
    controller = TheActivityController(userSession, Single.just(appLockConfig), lockAfterTimestamp)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(activity) }
  }

  @Test
  fun `when activity is started and user is logged out then app lock shouldn't be shown`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(false)

    uiEvents.onNext(TheActivityLifecycle.Started())

    verify(activity, never()).showAppLockScreen()
  }

  @Test
  fun `when activity is started, user is logged and in user was inactive then app lock should be shown`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(true)

    val lockAfterTime = Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(1))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    uiEvents.onNext(TheActivityLifecycle.Started())

    verify(activity).showAppLockScreen()
  }

  @Test
  fun `when app is stopped and lock timer is unset then the timer should be updated`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.MAX)
    whenever(lockAfterTimestamp.isSet).thenReturn(false)

    uiEvents.onNext(TheActivityLifecycle.Stopped())

    verify(lockAfterTimestamp).set(check {
      // Not the best way, but works.
      it > Instant.now().minusSeconds(1)
    })
  }

  @Test
  fun `when app is stopped and lock timer is set then the timer should not be updated`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(true)
    whenever(lockAfterTimestamp.isSet).thenReturn(true)
    whenever(lockAfterTimestamp.get()).thenReturn(Instant.now())

    uiEvents.onNext(TheActivityLifecycle.Stopped())

    verify(lockAfterTimestamp, never()).set(any())
  }

  @Test
  fun `when app is started unlocked and lock timer hasn't expired yet then the timer should be unset`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(true)

    val lockAfterTime = Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(10))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    uiEvents.onNext(TheActivityLifecycle.Started())

    verify(lockAfterTimestamp).delete()
  }

  @Test
  fun `when app is started locked and lock timer hasn't expired yet then the timer should not be unset`() {
    whenever(userSession.isUserLoggedIn()).thenReturn(true)

    val lockAfterTime = Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(5))
    whenever(lockAfterTimestamp.get()).thenReturn(lockAfterTime)

    uiEvents.onNext(TheActivityLifecycle.Started())

    verify(lockAfterTimestamp, never()).delete()
  }
}
