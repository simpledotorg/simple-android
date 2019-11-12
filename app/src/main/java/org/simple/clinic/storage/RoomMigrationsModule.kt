package org.simple.clinic.storage

import androidx.room.migration.Migration
import dagger.Module
import dagger.Provides

@Module
class RoomMigrationsModule {

  @Provides
  fun databaseMigrations(): List<Migration> {
    return listOf(
        Migration_3_4(),
        Migration_4_5(),
        Migration_5_6(),
        Migration_6_7(),
        Migration_7_8(),
        Migration_8_9(),
        Migration_9_10(),
        Migration_10_11(),
        Migration_11_12(),
        Migration_12_13(),
        Migration_13_14(),
        Migration_14_15(),
        Migration_15_16(),
        Migration_16_17(),
        Migration_17_18(),
        Migration_18_19(),
        Migration_19_20(),
        Migration_20_21(),
        Migration_21_22(),
        Migration_22_23(),
        Migration_23_24(),
        Migration_24_25(),
        Migration_25_26(),
        Migration_26_27(),
        Migration_27_28(),
        Migration_28_29(),
        Migration_29_30(),
        Migration_30_31(),
        Migration_31_32(),
        Migration_32_33(),
        Migration_33_34(),
        Migration_34_35(),
        Migration_35_36(),
        Migration_36_37(),
        Migration_37_38(),
        Migration_38_39(),
        Migration_39_40(),
        Migration_40_41(),
        Migration_41_42(),
        Migration_42_43(),
        Migration_43_44(),
        Migration_44_45(),
        Migration_45_46(),
        Migration_46_47(),
        Migration_47_48(),
        Migration_48_49(),
        Migration_49_50(),
        Migration_50_51(),
        Migration_51_52(),
        Migration_52_53()
    )
  }
}
