package org.simple.clinic.summary.teleconsultation.sync

import io.reactivex.Completable
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncTag
import org.simple.clinic.util.read
import javax.inject.Inject
import javax.inject.Named

class TeleconsultationSync @Inject constructor(
    private val repository: TeleconsultationFacilityRepository,
    private val api: TeleconsultFacilityInfoApi,
    @Named("sync_config_daily") private val config: SyncConfig
) : ModelSync {

  override val name: String = "TeleconsultationFacilityInfo"

  override val requiresSyncApprovedUser = true

  override fun sync(): Completable {
    return Completable
        .mergeArrayDelayError(
            Completable.fromAction { push() },
            Completable.fromAction { pull() }
        )
  }

  override fun push() {
    /* Nothing to do here */
  }

  override fun pull() {
    val teleconsultationInfoPullResponse = api.pull().execute().read()!!
    repository.mergeWithLocalData(teleconsultationInfoPullResponse.payloads)
  }

  override fun syncConfig() = config

  override fun syncTag() = SyncTag.DAILY
}
