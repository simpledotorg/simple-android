package org.simple.clinic.drugs.search.sync

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.drugs.search.DrugRepository
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastDrugPullToken
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import java.util.Optional
import javax.inject.Inject

class DrugSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: DrugRepository,
    private val api: DrugSyncApi,
    @TypedPreference(LastDrugPullToken) private val lastPullToken: Preference<Optional<String>>,
    @SyncConfigType(Frequent) private val config: SyncConfig
) : ModelSync {

  override val name = "Medication"

  override val requiresSyncApprovedUser = false

  override fun push() {
    // no-op
  }

  override fun pull() {
    val batchSize = config.pullBatchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) {
      api.pull(batchSize, it).execute().read()!!
    }
  }

}
