package org.simple.clinic.bp.di

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.sync.BloodPressureSyncApi
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import javax.inject.Named

@Module
open class BloodPressureModule {

  @Provides
  fun dao(appDatabase: AppDatabase): BloodPressureMeasurement.RoomDao {
    return appDatabase.bloodPressureDao()
  }

  @Provides
  fun syncApi(@Named("for_country") retrofit: Retrofit): BloodPressureSyncApi {
    return retrofit.create(BloodPressureSyncApi::class.java)
  }

  @Provides
  @Named("last_bp_pull_token")
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_bp_pull_token_v2", StringPreferenceConverter())
  }

  @Provides
  fun userInputDatePaddingCharacter(): UserInputDatePaddingCharacter {
    return UserInputDatePaddingCharacter.ZERO
  }
}
