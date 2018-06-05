package org.resolvetosavelives.red.sync

import android.app.Application
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.util.InstantRxPreferencesConverter
import org.resolvetosavelives.red.util.None
import org.resolvetosavelives.red.util.Optional
import org.resolvetosavelives.red.util.OptionalRxPreferencesConverter
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import retrofit2.Retrofit
import javax.inject.Named

@Module
open class SyncModule {

  @Provides
  fun patientSyncApi(appContext: Application, commonRetrofitBuilder: Retrofit.Builder): SyncApiV1 {
    val baseUrl = appContext.getString(R.string.redapp_endpoint)

    return commonRetrofitBuilder
        .baseUrl(baseUrl)
        .build()
        .create(SyncApiV1::class.java)
  }

  @Provides
  open fun patientSyncConfig(): Single<SyncConfig> {
    // In the future, this may come from the server.
    return Single.just(SyncConfig(frequency = Duration.ofHours(1), batchSize = 50))
  }

  @Provides
  @Named("last_patient_pull_timestamp")
  fun lastPullTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Optional<Instant>> {
    return rxSharedPrefs.getObject("last_patient_pull_timestamp", None, OptionalRxPreferencesConverter(InstantRxPreferencesConverter()))
  }
}
