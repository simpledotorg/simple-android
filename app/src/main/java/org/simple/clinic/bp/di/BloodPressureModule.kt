package org.simple.clinic.bp.di

import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.util.UserInputDatePaddingCharacter

@Module
open class BloodPressureModule {

  @Provides
  fun dao(appDatabase: AppDatabase): BloodPressureMeasurement.RoomDao {
    return appDatabase.bloodPressureDao()
  }

  @Provides
  fun userInputDatePaddingCharacter(): UserInputDatePaddingCharacter {
    return UserInputDatePaddingCharacter.ZERO
  }
}
