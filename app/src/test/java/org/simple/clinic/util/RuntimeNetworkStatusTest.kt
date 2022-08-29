package org.simple.clinic.util

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Test
import org.simple.clinic.analytics.NetworkCapabilitiesProvider
import org.simple.clinic.analytics.NetworkConnectivityStatus
import org.simple.clinic.analytics.NetworkConnectivityStatus.ACTIVE
import org.simple.clinic.analytics.NetworkConnectivityStatus.INACTIVE
import org.simple.clinic.util.RuntimeNetworkStatusTest.Event.FirstEvent
import org.simple.clinic.util.RuntimeNetworkStatusTest.Event.SecondEvent
import org.simple.clinic.util.RuntimeNetworkStatusTest.Event.ThirdEvent
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.Optional

class RuntimeNetworkStatusTest {

  private val events = PublishSubject.create<Event>()
  private val networkCapabilitiesProvider = mock<NetworkCapabilitiesProvider>()

  private val receivedEvents = events
      .compose(RuntimeNetworkStatus<Event>(
          networkCapabilitiesProvider = networkCapabilitiesProvider,
          schedulersProvider = TestSchedulersProvider.trampoline()
      ))
      .test()

  @After
  fun tearDown() {
    receivedEvents.dispose()
  }

  @Test
  fun `events should be forwarded when network status is not active`() {
    // given
    whenever(networkCapabilitiesProvider.networkConnectivityStatus()) doReturn INACTIVE

    // when
    events.onNext(FirstEvent)
    events.onNext(SecondEvent())
    events.onNext(ThirdEvent)

    // then
    receivedEvents
        .assertValues(
            FirstEvent,
            SecondEvent(Optional.of(INACTIVE)),
            ThirdEvent
        )
        .assertNotTerminated()
  }

  @Test
  fun `events should be forwarded when network status is active`() {
    // given
    whenever(networkCapabilitiesProvider.networkConnectivityStatus()) doReturn ACTIVE

    // when
    events.onNext(FirstEvent)
    events.onNext(SecondEvent())
    events.onNext(ThirdEvent)

    // then
    receivedEvents
        .assertValues(
            FirstEvent,
            SecondEvent(Optional.of(ACTIVE)),
            ThirdEvent
        )
        .assertNotTerminated()
  }

  sealed class Event {

    object FirstEvent : Event()

    data class SecondEvent(
        override var networkStatus: Optional<NetworkConnectivityStatus> = Optional.empty()
    ) : Event(), RequiresNetwork

    object ThirdEvent : Event()
  }
}
