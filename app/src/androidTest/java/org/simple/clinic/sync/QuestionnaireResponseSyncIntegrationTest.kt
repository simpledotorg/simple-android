package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastQuestionnaireResponsePullToken
import org.simple.clinic.questionnaire.MonthlyScreeningReports
import org.simple.clinic.questionnaireresponse.QuestionnaireResponseRepository
import org.simple.clinic.questionnaireresponse.sync.QuestionnaireResponseSync
import org.simple.clinic.questionnaireresponse.sync.QuestionnaireResponseSyncApi
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.unsafeLazy
import org.simple.sharedTestCode.TestData
import org.simple.sharedTestCode.util.Rules
import java.util.Optional
import java.util.UUID
import javax.inject.Inject

@Ignore("the review env doesn't contain any data")
class QuestionnaireResponseSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var repository: QuestionnaireResponseRepository

  @Inject
  @TypedPreference(LastQuestionnaireResponsePullToken)
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: QuestionnaireResponseSyncApi

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var syncInterval: SyncInterval

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())
      .around(SaveDatabaseRule())

  private lateinit var sync: QuestionnaireResponseSync

  private val batchSize = 3
  private lateinit var config: SyncConfig

  private val currentFacilityUuid: UUID by unsafeLazy { userSession.loggedInUserImmediate()!!.currentFacilityUuid }

  private val loggedInUser: User by unsafeLazy { userSession.loggedInUserImmediate()!! }

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)

    resetLocalData()

    config = SyncConfig(
        syncInterval = syncInterval,
        pullBatchSize = batchSize,
        pushBatchSize = batchSize,
        name = ""
    )

    sync = QuestionnaireResponseSync(
        syncCoordinator = SyncCoordinator(),
        api = syncApi,
        repository = repository,
        lastPullToken = lastPullToken,
        config = config
    )
  }

  private fun resetLocalData() {
    clearQuestionnaireResponseData()
    lastPullToken.delete()
  }

  private fun clearQuestionnaireResponseData() {
    appDatabase.questionnaireResponseDao().clear()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    sync.pull()
    val questionnaireResponseList = repository.questionnaireResponsesByType(MonthlyScreeningReports).blockingFirst()

    val updatedQuestionnaireResponseList = questionnaireResponseList.map {
      TestData.questionnaireResponse(
          uuid = it.uuid,
          questionnaireId = it.questionnaireId,
          questionnaireType = it.questionnaireType,
          facilityId = currentFacilityUuid
      )
    }

    Truth.assertThat(updatedQuestionnaireResponseList).containsNoDuplicates()

    updatedQuestionnaireResponseList.forEach {
      repository.updateQuestionnaireResponse(loggedInUser, it)
    }

    Truth.assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(updatedQuestionnaireResponseList.count())

    sync.push()

    Truth.assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(0)

    val modifiedRecord = updatedQuestionnaireResponseList[1].copy(
        content = mapOf(
            "monthly_screening_reports.new_field" to 10,
        )
    )
    repository.updateQuestionnaireResponse(loggedInUser, modifiedRecord)
    Truth.assertThat(repository.pendingSyncRecordCount().blockingFirst()).isEqualTo(1)
  }
}
