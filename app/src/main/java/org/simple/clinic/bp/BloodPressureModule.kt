package org.simple.clinic.bp

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Lazy
import dagger.Module
import dagger.Provides
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.bp.entry.BloodPressureEntrySheetController
import org.simple.clinic.bp.entry.BloodPressureEntrySheetControllerV2
import org.simple.clinic.bp.entry.UiChange
import org.simple.clinic.bp.sync.BloodPressureSyncApiV2
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.simple.clinic.util.StringPreferenceConverter
import org.simple.clinic.util.UserInputDatePaddingCharacter
import org.simple.clinic.widgets.UiEvent
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Named

@Module
open class BloodPressureModule {

  @Provides
  fun dao(appDatabase: AppDatabase): BloodPressureMeasurement.RoomDao {
    return appDatabase.bloodPressureDao()
  }

  @Provides
  fun syncApi(retrofit: Retrofit): BloodPressureSyncApiV2 {
    return retrofit.create(BloodPressureSyncApiV2::class.java)
  }

  @Provides
  @Named("last_bp_pull_token")
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("last_bp_pull_token_v2", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }

  /**
   * This is bad. Configs should be provided asynchronously because they'll be persisted in the
   * future, but injecting [BloodPressureEntrySheet] controller asynchronously becomes verbose.
   * Considering that this will get removed very soon once [BloodPressureConfig.dateEntryEnabled]
   * is enabled, I guess this is okay for now.
   */
  @Provides
  open fun provideBloodPressureEntryConfig(): BloodPressureConfig {
    return BloodPressureConfig(dateEntryEnabled = true)
  }

  @Provides
  fun provideBloodPressureEntryConfigProvider(config: BloodPressureConfig): Single<BloodPressureConfig> {
    return Single.just(config)
  }

  @Provides
  @Named("bp_entry_controller")
  fun controller(
      controllerV1: Lazy<BloodPressureEntrySheetController>,
      controllerV2: Lazy<BloodPressureEntrySheetControllerV2>,
      config: BloodPressureConfig
  ): ObservableTransformer<UiEvent, UiChange> {
    return when {
      config.dateEntryEnabled -> controllerV2.get()
      else -> controllerV1.get()
    }
  }

  @Provides
  fun userInputDatePaddingCharacter(locale: Locale): UserInputDatePaddingCharacter {
    return when (locale) {
      Locale.ENGLISH -> UserInputDatePaddingCharacter('0')
      else -> throw UnsupportedOperationException()
    }
  }
}
