package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Instant

@Entity
data class PatientAddress(
    @PrimaryKey
    val uuid: String,

    val colonyOrVillage: String,

    val district: String,

    val state: String,

    //todo: Don't use India as the country for everyone!
    val country: String? = "India",

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncPending: Boolean
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(address: PatientAddress)
  }
}
