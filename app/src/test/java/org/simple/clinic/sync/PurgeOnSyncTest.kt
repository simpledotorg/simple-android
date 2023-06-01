package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.junit.Test
import org.simple.clinic.AppDatabase
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

class PurgeOnSyncTest {

  private val clock = TestUtcClock(LocalDate.parse("2020-01-01"))
  private val now = Instant.now(clock)
  private val facility = TestData.facility(uuid = UUID.fromString("d12d4595-8adb-41ed-9492-c74f12d85ab6"))
  private val appDatabase = mock<AppDatabase>()
  private val delayPurgeAfterSwitchFor = Duration.ofHours(1)
  private val facilitySyncGroupSwitchedAt = mock<Preference<Optional<Instant>>>()

  private val purgeOnSync = PurgeOnSync(
      currentFacility = { facility },
      appDatabase = appDatabase,
      facilitySyncGroupSwitchedAt = facilitySyncGroupSwitchedAt,
      delayPurgeAfterSwitchFor = delayPurgeAfterSwitchFor,
      clock = clock
  )

  @Test
  fun `when the facility sync group has not been switched before, purging the data should happen`() {
    // given
    whenever(facilitySyncGroupSwitchedAt.get()).thenReturn(Optional.empty())

    // when
    purgeOnSync.purgeUnusedData()

    // then
    verify(appDatabase).deletePatientsNotInFacilitySyncGroup(facility)
    verifyNoMoreInteractions(appDatabase)
  }

  @Test
  fun `when the facility sync group has been switched, purging the data should not be done if the specified delay duration has not yet passed`() {
    // given
    val facilityGroupSwitchedAtInstant = now.minus(delayPurgeAfterSwitchFor)
    whenever(facilitySyncGroupSwitchedAt.get()).thenReturn(Optional.of(facilityGroupSwitchedAtInstant))

    // when
    purgeOnSync.purgeUnusedData()

    // then
    verify(appDatabase, never()).deletePatientsNotInFacilitySyncGroup(any())
    verifyNoMoreInteractions(appDatabase)
  }

  @Test
  fun `when the facility sync group has been switched, purging the data should be done if the specified delay duration has passed`() {
    // given
    val facilityGroupSwitchedAtInstant = now
        .minus(delayPurgeAfterSwitchFor)
        .minusSeconds(1)
    whenever(facilitySyncGroupSwitchedAt.get()).thenReturn(Optional.of(facilityGroupSwitchedAtInstant))

    // when
    purgeOnSync.purgeUnusedData()

    // then
    verify(appDatabase).deletePatientsNotInFacilitySyncGroup(facility)
    verifyNoMoreInteractions(appDatabase)
  }

}
