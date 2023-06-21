package org.simple.clinic.storage

import android.app.Application
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppScope

@Module
object EncryptionModule {

  @Provides
  @AppScope
  fun masterKey(appContext: Application): MasterKey {
    return MasterKey.Builder(appContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
  }
}
