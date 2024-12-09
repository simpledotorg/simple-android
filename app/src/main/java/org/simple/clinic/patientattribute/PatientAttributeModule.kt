package org.simple.clinic.patientattribute

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LastPatientAttributePullToken
import org.simple.clinic.patientattribute.sync.PatientAttributeSyncApi
import org.simple.clinic.util.preference.StringPreferenceConverter
import org.simple.clinic.util.preference.getOptional
import retrofit2.Retrofit
import java.util.Optional
import javax.inject.Named

@Module
class PatientAttributeModule {
  @Provides
  fun dao(appDatabase: AppDatabase): PatientAttribute.RoomDao {
    return appDatabase.patientAttributeDao()
  }

  @Provides
  fun syncApi(@Named("for_deployment") retrofit: Retrofit): PatientAttributeSyncApi {
    return retrofit.create(PatientAttributeSyncApi::class.java)
  }

  @Provides
  @TypedPreference(LastPatientAttributePullToken)
  fun lastPullToken(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getOptional("last_patient_attribute_pull_token", StringPreferenceConverter())
  }
}
