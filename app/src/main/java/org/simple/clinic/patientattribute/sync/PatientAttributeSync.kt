package org.simple.clinic.patientattribute.sync

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastPatientAttributePullToken
import org.simple.clinic.patientattribute.PatientAttribute
import org.simple.clinic.patientattribute.PatientAttributeRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncConfigType.Type.Frequent
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import java.util.Optional
import javax.inject.Inject

class PatientAttributeSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val repository: PatientAttributeRepository,
    private val api: PatientAttributeSyncApi,
    @TypedPreference(LastPatientAttributePullToken) private val lastPullToken: Preference<Optional<String>>,
    @SyncConfigType(Frequent) private val config: SyncConfig
) : ModelSync {

  override val name: String = "Patient Attribute"

  override val requiresSyncApprovedUser = true

  override fun push() {
    syncCoordinator.push(repository, config.pushBatchSize) { api.push(toRequest(it)).execute().read()!! }
  }

  override fun pull() {
    val batchSize = config.pullBatchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().read()!! }
  }

  private fun toRequest(patientAttributes: List<PatientAttribute>): PatientAttributePushRequest {
    val payloads = patientAttributes
        .map {
          it.run {
            PatientAttributePayload(
                uuid = uuid,
                patientUuid = patientUuid,
                userUuid = userUuid,
                height = bmiReading.height,
                weight = bmiReading.weight,
                createdAt = timestamps.createdAt,
                updatedAt = timestamps.updatedAt,
                deletedAt = timestamps.deletedAt
            )
          }
        }
    return PatientAttributePushRequest(payloads)
  }
}
