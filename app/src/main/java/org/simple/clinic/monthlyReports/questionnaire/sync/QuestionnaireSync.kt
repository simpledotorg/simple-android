package org.simple.clinic.monthlyReports.questionnaire.sync

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastQuestionnairePullToken
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireRepository
import org.simple.clinic.sync.ModelSync
import org.simple.clinic.sync.SyncConfig
import org.simple.clinic.sync.SyncConfigType
import org.simple.clinic.sync.SyncCoordinator
import org.simple.clinic.util.read
import java.util.Optional
import javax.inject.Inject

class QuestionnaireSync @Inject constructor(
    private val syncCoordinator: SyncCoordinator,
    private val api: QuestionnaireSyncApi,
    private val repository: QuestionnaireRepository,
    @TypedPreference(LastQuestionnairePullToken) private val lastPullToken: Preference<Optional<String>>,
    @SyncConfigType(SyncConfigType.Type.Frequent) private val config: SyncConfig
) : ModelSync {

  override val name: String = "Questionnaire"

  //Todo Sid - what should be the value for this boolean
  override val requiresSyncApprovedUser = true

  override fun push() {
    /* Nothing to do here */
  }

  override fun pull() {
    val batchSize = config.pullBatchSize

    //Todo Sid - Dsl version will be a global constant stored in the app
    val dslVersion = "v1"
    syncCoordinator.pull(repository, lastPullToken, batchSize) { api.pull(batchSize, dslVersion, it).execute().read()!! }
  }
}
