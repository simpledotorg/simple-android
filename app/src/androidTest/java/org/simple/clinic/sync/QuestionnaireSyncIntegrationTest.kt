package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth
import com.squareup.moshi.Moshi
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastQuestionnairePullToken
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireRepository
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireType
import org.simple.clinic.monthlyReports.questionnaire.sync.QuestionnairePullResponse
import org.simple.clinic.monthlyReports.questionnaire.sync.QuestionnaireSync
import org.simple.clinic.monthlyReports.questionnaire.sync.QuestionnaireSyncApi
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.util.moshi.InstantMoshiAdapter
import org.simple.clinic.util.moshi.LocalDateMoshiAdapter
import org.simple.clinic.util.moshi.QuestionnairePolymorphicJsonAdapterFactoryProvider
import org.simple.clinic.util.moshi.UuidMoshiAdapter
import org.simple.sharedTestCode.util.Rules
import java.util.Optional
import javax.inject.Inject

//@Ignore("the qa api is under development")
class QuestionnaireSyncIntegrationTest {

  @Inject
  lateinit var appDatabase: AppDatabase

  @Inject
  lateinit var repository: QuestionnaireRepository

  @Inject
  @TypedPreference(LastQuestionnairePullToken)
  lateinit var lastPullToken: Preference<Optional<String>>

  @Inject
  lateinit var syncApi: QuestionnaireSyncApi

  @Inject
  lateinit var syncInterval: SyncInterval

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      //      .around(ServerAuthenticationRule())
      .around(SaveDatabaseRule())

  private lateinit var sync: QuestionnaireSync

  private val batchSize = 1000
  private lateinit var config: SyncConfig

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

    sync = QuestionnaireSync(
        syncCoordinator = SyncCoordinator(),
        api = syncApi,
        repository = repository,
        lastPullToken = lastPullToken,
        config = config
    )
  }

  private fun resetLocalData() {
    clearQuestionnaireData()
    lastPullToken.delete()
  }

  private fun clearQuestionnaireData() {
    appDatabase.questionnaireDao().clearData()
  }

  @Test
  fun syncing_records_should_work_as_expected() {
    // when
    Truth.assertThat(repository.recordCount().blockingFirst()).isEqualTo(0)

//    val moshi = Moshi.Builder()
//        .add(InstantMoshiAdapter())
//        .add(LocalDateMoshiAdapter())
//        .add(UuidMoshiAdapter())
//        .add(QuestionnaireType.MoshiTypeAdapter())
//        .add(QuestionnairePolymorphicJsonAdapterFactoryProvider().getFactory()).build()
//    //      val processToken = lastPullToken.get().toNullable()
//    //
//    //      val response = pullNetworkCall(processToken)
//
//    val json = """
//{
//    "questionnaires": [
//        {
//            "id": "497f6eca-6276-4993-bfeb-53cbbbba6f08",
//            "deleted_at": "2019-08-24T14:15:22Z",
//            "created_at": "2019-08-24T14:15:22Z",
//            "questionnaire_type": "monthly_screening_reports",
//            "layout": {
//  "item": [
//    {
//      "link_id": "monthly_opd_visits",
//      "text": "Monthly OPD visits for adults >30 years old",
//      "type": "group",
//      "display": {
//        "view_type": "sub_header",
//        "orientation": "vertical"
//      },
//      "item": [
//        {
//          "link_id": "outpatient_department_visits",
//          "text": "Outpatient department visits",
//          "type": "integer",
//          "validations": {
//            "min": 0,
//            "max": 1000000
//          },
//          "display": {
//            "view_type": "input_field",
//            "orientation": "vertical"
//          }
//        }
//      ]
//    },
//    {
//      "link_id": "htm_and_dm_screening",
//      "text": "HTN & DM SCREENING",
//      "type": "group",
//      "display": {
//        "view_type": "header",
//        "orientation": "vertical"
//      },
//      "item": [
//        {
//          "link_id": "total_bp_checks",
//          "text": "Total BP Checks done",
//          "type": "group",
//          "display": {
//            "view_type": "sub_header",
//            "orientation": "horizontal"
//          },
//          "item": [
//            {
//              "link_id": "blood_pressure_checks_male",
//              "text": "Male",
//              "type": "integer",
//              "validations": {
//                "min": 0,
//                "max": 1000000
//              },
//              "display": {
//                "view_type": "input_field"
//              }
//            },
//            {
//              "link_id": "blood_pressure_checks_female",
//              "text": "Female",
//              "type": "integer",
//              "validations": {
//                "min": 0,
//                "max": 1000000
//              },
//              "display": {
//                "view_type": "input_field"
//              }
//            }
//          ]
//        },
//        {
//          "link_id": "total_blood_sugar_checks",
//          "text": "Total blood sugar checks done",
//          "type": "group",
//          "display": {
//            "view_type": "sub_header",
//            "orientation": "horizontal"
//          },
//          "item": [
//            {
//              "link_id": "blood_sugar_checks_male",
//              "text": "Male",
//              "type": "integer",
//              "display": {
//                "view_type": "input_field"
//              },
//              "validations": {
//                "min": 0,
//                "max": 1000000
//              }
//            },
//            {
//              "link_id": "blood_sugar_checks_female",
//              "text": "Female",
//              "type": "integer",
//              "display": {
//                "view_type": "input_field"
//              },
//              "validations": {
//                "min": 0,
//                "max": 1000000
//              }
//            },
//            {
//              "link_id": "blood_sugar_checks_transgender",
//              "text": "Transgender",
//              "type": "integer",
//              "display": {
//                "view_type": "input_field"
//              },
//              "validations": {
//                "min": 0,
//                "max": 1000000
//              }
//            }
//          ]
//        }
//      ]
//    }
//  ]
//}
//        }
//    ],
//    "process_token": "string"
//}
//    """.trimIndent()
//
//    val adapter =
//        moshi.adapter(QuestionnairePullResponse::class.java)
//    val response: QuestionnairePullResponse = adapter.fromJson(json) ?: QuestionnairePullResponse(payloads = listOf(), processToken = "")
//
//    repository.mergeWithLocalData(response.payloads)
          sync.pull()

    // then
    val pulledRecords = repository.questionnaires()

    Truth.assertThat(pulledRecords).isNotEmpty()
  }
}
