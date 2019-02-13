package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RxErrorsRule
import org.threeten.bp.Instant
import java.util.UUID

class SyncCoordinatorTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private lateinit var syncCoordinator: SyncCoordinator
  private lateinit var repository: SynceableRepository<Any, Any>
  private lateinit var lastSyncTimestamp: Preference<Optional<Instant>>

  @Before
  fun setup() {
    repository = mock()
    lastSyncTimestamp = mock()

    syncCoordinator = SyncCoordinator()
  }

  @Test
  fun `when pending sync records are empty, then the push network call should not be made`() {
    whenever(repository.recordsWithSyncStatus(SyncStatus.PENDING)).thenReturn(Single.just(emptyList()))

    var networkCallMade = false

    syncCoordinator.push(repository) {
      networkCallMade = true
      Single.just(DataPushResponse(emptyList()))
    }.blockingAwait()

    assertThat(networkCallMade).isFalse()
  }

  @Test
  fun `when a push succeeds then any records that were created or updated during the push started should not get marked as synced`() {
    // TODO: This is tricky to test since it involves concurrency. See if we can change how
    // the sync itself happens so that this case does not get triggered.
  }

  @Test
  fun `if there are validation errors in push, then the failing records should be marked as invalid`() {
    whenever(repository.recordsWithSyncStatus(SyncStatus.PENDING)).thenReturn(Single.just(listOf(1, 2, 3)))
    whenever(repository.setSyncStatus(any<SyncStatus>(), any())).thenReturn(Completable.complete())
    whenever(repository.setSyncStatus(any<List<UUID>>(), any())).thenReturn(Completable.complete())

    val validationErrors = listOf(
        ValidationErrors(uuid = UUID.randomUUID(), schemaErrorMessages = listOf("error-1")),
        ValidationErrors(uuid = UUID.randomUUID(), schemaErrorMessages = listOf("error-2"))
    )

    syncCoordinator.push(repository) {
      Single.just(DataPushResponse(validationErrors))
    }.blockingAwait()

    verify(repository).setSyncStatus(validationErrors.map { it.uuid }, SyncStatus.INVALID)
  }
}
