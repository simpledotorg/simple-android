package org.resolvetosavelives.red.sync

import android.app.Application
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.util.InstantRxPreferencesConverter
import org.resolvetosavelives.red.util.OptionalRxPreferencesConverter
import org.threeten.bp.Duration
import org.threeten.bp.Instant
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
    return Single.just(PatientSyncConfig(frequency = Duration.ofHours(1), batchSize = 50))
  }

  @Provides
  @Named("last_pull_timestamp")
  fun lastPullTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Optional<Instant>> {
    return rxSharedPrefs.getObject("last_pull_timestamp", None, OptionalRxPreferencesConverter(InstantRxPreferencesConverter()))
  }
}
