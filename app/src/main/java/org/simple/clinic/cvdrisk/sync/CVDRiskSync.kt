package org.simple.clinic.cvdrisk.sync

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.cvdrisk.CVDRisk
import org.simple.clinic.cvdrisk.CVDRiskRepository
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastCVDRiskPullToken
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import java.util.Optional
import javax.inject.Inject

class CVDRiskSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: CVDRiskRepository,
    private val api: CVDRiskSyncApi,
    @TypedPreference(LastCVDRiskPullToken) private val lastPullToken: Preference<Optional<String>>,
    @SyncConfigType(Frequent) private val config: SyncConfig
) : ModelSync {

    override val name: String = "CVD Risk"

    override val requiresSyncApprovedUser = true

    override fun push() {
        syncCoordinator.push(repository, config.pushBatchSize) {
            api.push(toRequest(it)).execute().read()!!
        }
    }

    override fun pull() {
        val batchSize = config.pullBatchSize
        syncCoordinator.pull(repository, lastPullToken, batchSize) {
            api.pull(batchSize, it).execute().read()!!
        }
    }

    private fun toRequest(cvdRisks: List<CVDRisk>): CVDRiskPushRequest {
        val payloads = cvdRisks
            .map {
                it.run {
                    CVDRiskPayload(
                        uuid = uuid,
                        patientUuid = patientUuid,
                        riskScore = riskScore,
                        createdAt = timestamps.createdAt,
                        updatedAt = timestamps.updatedAt,
                        deletedAt = timestamps.deletedAt
                    )
                }
            }
        return CVDRiskPushRequest(payloads)
    }
}
