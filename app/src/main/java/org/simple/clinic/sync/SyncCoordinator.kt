package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireComponentType
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireType
import org.simple.clinic.monthlyReports.questionnaire.component.BaseComponent
import org.simple.clinic.monthlyReports.questionnaire.sync.QuestionnairePullResponse
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.moshi.InstantMoshiAdapter
import org.simple.clinic.util.moshi.LocalDateMoshiAdapter
import org.simple.clinic.util.moshi.QuestionnairePolymorphicJsonAdapterFactoryProvider
import org.simple.clinic.util.moshi.UuidMoshiAdapter
import org.simple.clinic.util.toNullable
import timber.log.Timber
import java.util.Optional
import java.util.UUID
import javax.inject.Inject

class SyncCoordinator @Inject constructor(
) {

  fun <T : Any, P> push(
      repository: SynceableRepository<T, P>,
      batchSize: Int,
      pushNetworkCall: (List<T>) -> DataPushResponse
  ) {
    var offset = 0
    var recordsToSync = repository.pendingSyncRecords(
        limit = batchSize,
        offset = 0
    )
    val recordIdsWithErrors = mutableListOf<UUID>()

    while (recordsToSync.isNotEmpty()) {
      val response = pushNetworkCall(recordsToSync)

      val validationErrors = response.validationErrors
      recordIdsWithErrors.addAll(validationErrors.map { it.uuid })

      logValidationErrors(validationErrors, recordsToSync)

      offset += recordsToSync.size
      recordsToSync = repository.pendingSyncRecords(
          limit = batchSize,
          offset = offset
      )
    }

    repository.setSyncStatus(SyncStatus.PENDING, SyncStatus.DONE)

    if (recordIdsWithErrors.isNotEmpty()) {
      repository.setSyncStatus(recordIdsWithErrors, SyncStatus.INVALID)
    }
  }

  private fun <T : Any> logValidationErrors(
      validationErrors: List<ValidationErrors>,
      pendingSyncRecords: List<T>
  ) {
    if (validationErrors.isNotEmpty()) {
      val recordType = pendingSyncRecords.first().javaClass.simpleName
      Timber.e("Server sent validation errors when syncing $recordType : $validationErrors")
    }
  }

  fun <T : Any, P> pull(
      repository: SynceableRepository<T, P>,
      lastPullToken: Preference<Optional<String>>,
      batchSize: Int,
      pullNetworkCall: (String?) -> DataPullResponse<P>
  ) {
    var hasFetchedAllData = false

    val moshi = Moshi.Builder()
        .add(InstantMoshiAdapter())
        .add(LocalDateMoshiAdapter())
        .add(UuidMoshiAdapter())
        .add(QuestionnaireType.MoshiTypeAdapter())
        .add(QuestionnairePolymorphicJsonAdapterFactoryProvider().getFactory()).build()
    while (!hasFetchedAllData) {
      //      val processToken = lastPullToken.get().toNullable()
      //
      //      val response = pullNetworkCall(processToken)

      val json = """
{
    "questionnaires": [
        {
            "id": "497f6eca-6276-4993-bfeb-53cbbbba6f08",
            "deleted_at": "2019-08-24T14:15:22Z",
            "created_at": "2019-08-24T14:15:22Z",
            "questionnaire_type": "string",
            "layout": {
                "item": [
                    {
                        "link_id": "monthly_opd_visits",
                        "text": "Monthly OPD visits for adults >30 years old",
                        "type": "group",
                        "display": {
                            "view_type": "sub_header",
                            "orientation": "vertical"
                        },
                        "item": [
                            {
                                "link_id": "outpatient_department_visits",
                                "text": "Outpatient department visits",
                                "type": "integer",
                                "validations": {
                                    "min": 0,
                                    "max": 1000000
                                },
                                "display": {
                                    "view_type": "input_field",
                                    "orientation": "vertical"
                                }
                            }
                        ]
                    },
                    {
                        "link_id": "htm_and_dm_screening",
                        "text": "HTN & DM SCREENING",
                        "type": "group",
                        "display": {
                            "view_type": "header_group",
                            "orientation": "vertical"
                        },
                        "item": [
                            {
                                "link_id": "total_bp_checks",
                                "text": "Total BP Checks done",
                                "type": "group",
                                "display": {
                                    "view_type": "sub_header",
                                    "orientation": "horizontal"
                                },
                                "item": [
                                    {
                                        "link_id": "blood_pressure_checks_male",
                                        "text": "Male",
                                        "type": "integer",
                                        "validations": {
                                            "min": 0,
                                            "max": 1000000
                                        },
                                        "display": {
                                            "view_type": "input_field"
                                        }
                                    },
                                    {
                                        "link_id": "blood_pressure_checks_female",
                                        "text": "Female",
                                        "type": "integer",
                                        "validations": {
                                            "min": 0,
                                            "max": 1000000
                                        },
                                        "display": {
                                            "view_type": "input_field"
                                        }
                                    }
                                ]
                            },
                            {
                                "link_id": "total_blood_sugar_checks",
                                "text": "Total blood sugar checks done",
                                "type": "integer",
                                "display": {
                                    "view_type": "sub_header_group",
                                    "orientation": "horizontal"
                                },
                                "item": [
                                    {
                                        "link_id": "blood_sugar_checks_male",
                                        "text": "Male",
                                        "type": "integer",
                                        "display": {
                                            "view_type": "input_field"
                                        },
                                        "validations": {
                                            "min": 0,
                                            "max": 1000000
                                        }
                                    },
                                    {
                                        "link_id": "blood_sugar_checks_female",
                                        "text": "Female",
                                        "type": "integer",
                                        "display": {
                                            "view_type": "input_field"
                                        },
                                        "validations": {
                                            "min": 0,
                                            "max": 1000000
                                        }
                                    },
                                    {
                                        "link_id": "blood_sugar_checks_transgender",
                                        "text": "Transgender",
                                        "type": "integer",
                                        "display": {
                                            "view_type": "input_field"
                                        },
                                        "validations": {
                                            "min": 0,
                                            "max": 1000000
                                        }
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        }
    ],
    "process_token": "string"
}
    """.trimIndent()

      val adapter =
          moshi.adapter(QuestionnairePullResponse::class.java)
      val response: QuestionnairePullResponse = adapter.fromJson(json) ?: QuestionnairePullResponse(payloads = listOf(), processToken = "")

      println(response)
//            repository.mergeWithLocalData(response.payloads)
//            lastPullToken.set(Optional.of(response.processToken))

      hasFetchedAllData = response.payloads.size < batchSize
    }
  }
}
