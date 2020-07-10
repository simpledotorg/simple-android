package org.simple.clinic.location

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.simple.clinic.location.LocationUpdate.Available
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.platform.util.RuntimePermissionResult.DENIED
import org.simple.clinic.platform.util.RuntimePermissionResult.GRANTED
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.TestElapsedRealtimeClock
import org.simple.clinic.util.advanceTimeBy
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Duration

class ScreenLocationUpdatesTest {

  private val runtimePermissions = mock<RuntimePermissions>()
  private val repository = mock<LocationRepository>()
  private val clock = TestElapsedRealtimeClock()

  private val phcObvious = Coordinates(latitude = 12.9653052, longitude = 77.59554)

  private lateinit var screenLocationUpdates: ScreenLocationUpdates

  @Test
  fun `when the location permission is denied, the location updates must not be setup`() {
    // given
    setup(permission = DENIED)

    // then
    screenLocationUpdates
        .streamUserLocation(
            updateInterval = Duration.ZERO,
            timeout = Duration.ZERO,
            discardOlderThan = Duration.ZERO
        )
        .test()
        .assertValue(Unavailable)
        .assertNoErrors()
        .dispose()

    verifyZeroInteractions(repository)
  }

  @Test
  fun `when the location permission is granted, the location updates must be setup`() {
    // given
    val updateInterval = Duration.ofMillis(1)
    val locationUpdate = Available(phcObvious, Duration.ZERO)
    whenever(repository.streamUserLocation(eq(updateInterval), any())) doReturn Observable.just<LocationUpdate>(locationUpdate)

    setup(
        permission = GRANTED,
        schedulers = TestSchedulersProvider.trampoline(computationScheduler = TestScheduler())
    )

    // then
    screenLocationUpdates
        .streamUserLocation(
            updateInterval = updateInterval,
            timeout = Duration.ZERO,
            discardOlderThan = Duration.ZERO
        )
        .test()
        .assertValue(locationUpdate)
        .assertNoErrors()
        .dispose()
  }

  @Test
  fun `if location update is received within the timeout interval, the timeout must be cancelled`() {
    // given
    val oneMillisecond = Duration.ofMillis(1)
    @Suppress("UnnecessaryVariable") val updateInterval = oneMillisecond
    val timeoutInterval = Duration.ofMillis(3)
    val locationUpdate = Available(phcObvious, Duration.ZERO)

    val locationSubject = PublishSubject.create<LocationUpdate>()
    whenever(repository.streamUserLocation(eq(updateInterval), any())) doReturn locationSubject

    val timeoutScheduler = TestScheduler()
    setup(schedulers = TestSchedulersProvider.trampoline(computationScheduler = timeoutScheduler))

    // then
    val testObserver = screenLocationUpdates
        .streamUserLocation(
            updateInterval = updateInterval,
            timeout = timeoutInterval,
            discardOlderThan = Duration.ZERO
        )
        .test()

    timeoutScheduler.advanceTimeBy(oneMillisecond)
    testObserver
        .assertNoValues()
        .assertNoErrors()

    timeoutScheduler.advanceTimeBy(oneMillisecond)
    testObserver
        .assertNoValues()
        .assertNoErrors()

    locationSubject.onNext(locationUpdate)

    timeoutScheduler.advanceTimeBy(oneMillisecond)
    testObserver
        .assertValue(locationUpdate)
        .assertNoErrors()

    testObserver.dispose()
  }

  @Test
  fun `if location updates are not received within the timeout interval, the location must be signalled as unavailable`() {
    // given
    val oneMillisecond = Duration.ofMillis(1)
    @Suppress("UnnecessaryVariable") val updateInterval = oneMillisecond
    val timeoutInterval = Duration.ofMillis(3)

    whenever(repository.streamUserLocation(eq(updateInterval), any())) doReturn Observable.never<LocationUpdate>()

    val timeoutScheduler = TestScheduler()
    setup(schedulers = TestSchedulersProvider.trampoline(computationScheduler = timeoutScheduler))

    // then
    val testObserver = screenLocationUpdates
        .streamUserLocation(
            updateInterval = updateInterval,
            timeout = timeoutInterval,
            discardOlderThan = Duration.ZERO
        )
        .test()

    timeoutScheduler.advanceTimeBy(oneMillisecond)
    testObserver
        .assertNoValues()
        .assertNoErrors()

    timeoutScheduler.advanceTimeBy(oneMillisecond)
    testObserver
        .assertNoValues()
        .assertNoErrors()

    timeoutScheduler.advanceTimeBy(oneMillisecond)
    testObserver
        .assertValue(Unavailable)
        .assertNoErrors()

    testObserver.dispose()
  }

  @Test
  fun `stale location updates must be ignored`() {
    // given
    val updateInterval = Duration.ofMillis(1)

    val justAfterStaleInterval = Available(location = phcObvious, timeSinceBootWhenRecorded = Duration.ofMillis(4))
    val exactlyAtStaleInterval = Available(location = phcObvious, timeSinceBootWhenRecorded = Duration.ofMillis(5))
    val justBeforeStaleInterval = Available(location = phcObvious, timeSinceBootWhenRecorded = Duration.ofMillis(6))

    whenever(repository.streamUserLocation(eq(updateInterval), any())) doReturn Observable.just<LocationUpdate>(
        justAfterStaleInterval,
        exactlyAtStaleInterval,
        justBeforeStaleInterval
    )

    setup(schedulers = TestSchedulersProvider.trampoline(computationScheduler = TestScheduler()))
    clock.advanceBy(Duration.ofMillis(10))

    // then
    val testObserver = screenLocationUpdates
        .streamUserLocation(
            updateInterval = updateInterval,
            timeout = Duration.ZERO,
            discardOlderThan = Duration.ofMillis(5)
        )
        .test()

    testObserver
        .assertValues(exactlyAtStaleInterval, justBeforeStaleInterval)
        .assertNoErrors()

    testObserver.dispose()
  }

  private fun setup(
      permission: RuntimePermissionResult = GRANTED,
      schedulers: SchedulersProvider = TestSchedulersProvider.trampoline()
  ) {
    whenever(runtimePermissions.check(LOCATION_PERMISSION)) doReturn permission

    screenLocationUpdates = ScreenLocationUpdates(
        locationRepository = repository,
        clock = clock,
        runtimePermissions = runtimePermissions,
        schedulers = schedulers
    )
  }
}
