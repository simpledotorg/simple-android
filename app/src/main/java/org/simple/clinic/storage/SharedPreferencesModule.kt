package org.simple.clinic.storage

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope
import org.simple.clinic.storage.SharedPreferencesMode.Mode.Default
import org.simple.clinic.storage.SharedPreferencesMode.Mode.Encrypted
import javax.inject.Qualifier

@Module
class SharedPreferencesModule {

  @Provides
  @AppScope
  fun rxSharedPreferences(@SharedPreferencesMode(Default) preferences: SharedPreferences): RxSharedPreferences {
    return RxSharedPreferences.create(preferences)
  }

  @Provides
  @AppScope
  @SharedPreferencesMode(Default)
  fun sharedPreferences(appContext: Application): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(appContext)
  }

  @Provides
  @AppScope
  @SharedPreferencesMode(Encrypted)
  fun encryptedSharedPreferences(
      appContext: Application,
      masterKey: MasterKey
  ): SharedPreferences {
    return EncryptedSharedPreferences.create(
        appContext,
        "simple_encrypted_preferences",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
  }
}

@Qualifier
annotation class SharedPreferencesMode(val mode: Mode) {

  enum class Mode {
    Default,
    Encrypted
  }
}
