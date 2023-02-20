package org.simple.clinic.questionnaireresponse.sync

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastQuestionnaireResponsePullToken
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import java.util.Optional
import javax.inject.Inject

class QuestionnaireResponseSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val api: QuestionnaireResponseSyncApi,
    private val repository: QuestionnaireResponseRepository,
    @TypedPreference(LastQuestionnaireResponsePullToken) private val lastPullToken: Preference<Optional<String>>,
    @SyncConfigType(SyncConfigType.Type.Frequent) private val config: SyncConfig
) : ModelSync {

  override val name: String = "Questionnaire Response"

  override val requiresSyncApprovedUser = true

  override fun push() {
    syncCoordinator.push(repository, config.pushBatchSize) { api.push(toRequest(it)).execute().read()!! }
  }

  override fun pull() {
    val batchSize = config.pullBatchSize
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, it).execute().read()!! }
  }

  private fun toRequest(responses: List<QuestionnaireResponse>): QuestionnaireResponsePushRequest {
    val payloads = responses.map { it.toPayload() }
    return QuestionnaireResponsePushRequest(payloads)
  }
}
