package org.simple.clinic.protocol

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.util.Optional
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import javax.inject.Named

@Module
class ProtocolModule {

  @Provides
  fun protocolDao(appDatabase: AppDatabase): Protocol.RoomDao {
    return appDatabase.protocolDao()
  }

  @Provides
  fun protocolDrugDao(appDatabase: AppDatabase): ProtocolDrug.RoomDao {
    return appDatabase.protocolDrugDao()
  }

  @Provides
  fun protocolSyncApi(@Named("for_country") retrofit: Retrofit): ProtocolSyncApi {
    return retrofit.create(ProtocolSyncApi::class.java)
  }

  @Provides
  @Named("last_protocol_pull_token")
  fun lastPullToken(rxSharedPreferences: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPreferences.getOptional("last_protocol_pull_timestamp", StringPreferenceConverter())
  }
}
