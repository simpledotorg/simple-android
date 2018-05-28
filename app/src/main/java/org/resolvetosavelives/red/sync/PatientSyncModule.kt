package org.resolvetosavelives.red.sync

import android.app.Application
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.resolvetosavelives.red.R
import org.threeten.bp.Duration
import retrofit2.Retrofit
import javax.inject.Named

@Module
class PatientSyncModule {

  @Provides
  fun patientSyncApi(appContext: Application, commonRetrofitBuilder: Retrofit.Builder): PatientSyncApiV1 {
    val baseUrl = appContext.getString(R.string.redapp_endpoint)

    return commonRetrofitBuilder
        .baseUrl(baseUrl)
        .build()
        .create(PatientSyncApiV1::class.java)
  }

  @Provides
  fun patientSyncConfig(): Single<PatientSyncConfig> {
    // In the future, this may come from the server.
    return Single.just(PatientSyncConfig(Duration.ofHours(1)))
  }

  @Provides
  @Named("first_patient_sync_done")
  fun firstSyncFlag(rxSharedPrefs: RxSharedPreferences): Preference<Boolean> {
    return rxSharedPrefs.getBoolean("first_patient_sync_done", false)
  }
}
