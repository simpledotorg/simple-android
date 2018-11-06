package org.simple.clinic.drugs.sync

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Completable
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

class PrescriptionSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val api: PrescriptionSyncApiV1,
    private val repository: PrescriptionRepository,
    @Named("last_prescription_pull_timestamp") private val lastPullTimestamp: Preference<Optional<Instant>>
) {

  fun sync(): Completable = Completable.mergeArrayDelayError(push(), pull())

  fun push() = syncCoordinator.push(repository) { api.push(toRequest(it)) }

  fun pull() = syncCoordinator.pull(repository, lastPullTimestamp, api::pull)

  private fun toRequest(drugs: List<PrescribedDrug>): PrescriptionPushRequest {
    val payloads = drugs.map { it.toPayload() }
    return PrescriptionPushRequest(payloads)
  }
}
