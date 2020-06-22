package org.simple.clinic.util

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Test
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.util.RequestPermissionsTest.Event.FirstEvent
import org.simple.clinic.util.RequestPermissionsTest.Event.FourthEvent
import org.simple.clinic.util.RequestPermissionsTest.Event.SecondEvent
import org.simple.clinic.util.RequestPermissionsTest.Event.ThirdEvent

class RequestPermissionsTest {

  private val events = PublishSubject.create<Event>()
  private val permissionResults = PublishSubject.create<ActivityPermissionResult>()

  private val runtimePermissions = mock<RuntimePermissions>()

  private val receivedEvents = events
      .compose(RequestPermissions<Event>(runtimePermissions, permissionResults))
      .test()

  @After
  fun tearDown() {
    receivedEvents.dispose()
  }

  @Test
  fun `events requiring permission should not be forwarded if the permission is denied`() {
    // given
    whenever(runtimePermissions.check("permission_1")) doReturn DENIED
    whenever(runtimePermissions.check("permission_2")) doReturn DENIED

    // when
    events.onNext(FirstEvent)
    events.onNext(SecondEvent())
    events.onNext(ThirdEvent)
    events.onNext(FourthEvent())
    events.onNext(FirstEvent)

    // then
    receivedEvents
        .assertValues(FirstEvent, ThirdEvent, FirstEvent)
        .assertNotTerminated()
  }

  @Test
  fun `events requiring permission should be forwarded if the current permission is granted`() {
    // given
    whenever(runtimePermissions.check("permission_1")) doReturn GRANTED
    whenever(runtimePermissions.check("permission_2")) doReturn GRANTED

    // when
    events.onNext(FirstEvent)
    events.onNext(SecondEvent())
    events.onNext(ThirdEvent)
    events.onNext(FourthEvent())
    events.onNext(FirstEvent)

    // then
    receivedEvents
        .assertValues(
            FirstEvent,
            SecondEvent(permission = Optional.of(GRANTED)),
            ThirdEvent,
            FourthEvent(permission = Optional.of(GRANTED)),
            FirstEvent
        )
        .assertNotTerminated()
  }

  @Test
  fun `permission should be requested if it currently is denied`() {
    whenever(runtimePermissions.check("permission_1")) doReturn DENIED
    whenever(runtimePermissions.check("permission_2")) doReturn DENIED

    // when
    events.onNext(FirstEvent)
    events.onNext(SecondEvent())
    events.onNext(ThirdEvent)
    events.onNext(FourthEvent())
    events.onNext(FirstEvent)

    // then
    verify(runtimePermissions).request("permission_1", 1)
    verify(runtimePermissions).request("permission_2", 2)
  }

  @Test
  fun `permission should not be requested if it currently is granted`() {
    whenever(runtimePermissions.check("permission_2")) doReturn GRANTED

    // when
    events.onNext(FirstEvent)
    events.onNext(SecondEvent())
    events.onNext(ThirdEvent)
    events.onNext(FourthEvent())
    events.onNext(FirstEvent)

    // then
    verify(runtimePermissions, never()).request("permission_2", 2)
  }

  @Test
  fun `when a permission is granted, the event should be forwarded`() {
    // given
    whenever(runtimePermissions.check("permission_1")).doReturn(DENIED, GRANTED)
    whenever(runtimePermissions.check("permission_2")).doReturn(DENIED, DENIED)

    // when
    events.onNext(FirstEvent)
    events.onNext(SecondEvent())
    events.onNext(ThirdEvent)
    events.onNext(FourthEvent())
    events.onNext(FirstEvent)

    // then
    permissionResults.onNext(ActivityPermissionResult(requestCode = 2))
    receivedEvents
        .assertValues(
            FirstEvent,
            ThirdEvent,
            FirstEvent,
            FourthEvent(permission = Optional.of(DENIED))
        )
        .assertNotTerminated()

    permissionResults.onNext(ActivityPermissionResult(requestCode = 1))
    receivedEvents
        .assertValues(
            FirstEvent,
            ThirdEvent,
            FirstEvent,
            FourthEvent(permission = Optional.of(DENIED)),
            SecondEvent(permission = Optional.of(GRANTED))
        )
        .assertNotTerminated()
  }

  sealed class Event {

    object FirstEvent : Event()

    data class SecondEvent(
        override var permission: Optional<RuntimePermissionResult> = None(),
        override val permissionRequestCode: Int = 1,
        override val permissionString: String = "permission_1"
    ) : Event(), RequiresPermission

    object ThirdEvent : Event()

    data class FourthEvent(
        override var permission: Optional<RuntimePermissionResult> = None(),
        override val permissionRequestCode: Int = 2,
        override val permissionString: String = "permission_2"
    ) : Event(), RequiresPermission
  }
}
