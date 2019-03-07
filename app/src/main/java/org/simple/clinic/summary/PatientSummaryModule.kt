package org.simple.clinic.summary

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.patient.PatientSummaryResult
import javax.inject.Named

@Module
open class PatientSummaryModule {

  @Provides
  open fun providesSummaryConfig(): Single<PatientSummaryConfig> = Single.just(PatientSummaryConfig(
      numberOfBpPlaceholders = 3,
      numberOfBpsToDisplay = 100
  ))

  @Provides
  @Named("patient_summary_result")
  fun patientSummaryResult(rxSharedPreferences: RxSharedPreferences, moshi: Moshi): Preference<PatientSummaryResult> {
    val typeConverter = PatientSummaryResult.RxPreferencesConverter(moshi)
    return rxSharedPreferences.getObject("patient_summary_result_v1", PatientSummaryResult.NotSaved, typeConverter)
  }
}
