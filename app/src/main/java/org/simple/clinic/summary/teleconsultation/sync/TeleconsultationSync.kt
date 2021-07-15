package org.simple.clinic.summary.teleconsultation.sync

import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.util.read
import javax.inject.Inject

class TeleconsultationSync @Inject constructor(
    private val repository: TeleconsultationFacilityRepository,
    private val api: TeleconsultFacilityInfoApi,
    @SyncConfigType(Frequent) private val config: SyncConfig
) : ModelSync {

  override val name: String = "TeleconsultationFacilityInfo"

  override val requiresSyncApprovedUser = true

  override fun push() {
    /* Nothing to do here */
  }

  override fun pull() {
    val teleconsultationInfoPullResponse = api.pull().execute().read()!!
    repository.mergeWithLocalData(teleconsultationInfoPullResponse.payloads)
  }

}
