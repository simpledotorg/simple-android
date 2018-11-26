package org.simple.clinic.patient

import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.Moshi
import java.util.UUID

sealed class PatientSummaryResult {

  data class Saved(val patientUuid: UUID) : PatientSummaryResult()

  object NotSaved : PatientSummaryResult()

  data class Scheduled(val patientUuid: UUID) : PatientSummaryResult()

  class RxPreferencesConverter(moshi: Moshi) : Preference.Converter<PatientSummaryResult> {

    private val adapter by lazy { moshi.adapter(PatientSummaryResult::class.java) }

    override fun deserialize(serialized: String): PatientSummaryResult {
      return adapter.fromJson(serialized)!!
    }

    override fun serialize(value: PatientSummaryResult): String {
      return adapter.toJson(value)
    }
  }
}
