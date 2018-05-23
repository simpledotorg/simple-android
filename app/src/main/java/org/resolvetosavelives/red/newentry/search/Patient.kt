package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

// TODO: find a better package for Patient and its related classes.

@Entity(
    foreignKeys = [
      ForeignKey(
          entity = PatientAddress::class,
          parentColumns = ["uuid"],
          childColumns = ["addressUuid"])
    ],
    indices = [
      Index("addressUuid", unique = true)
    ])
data class Patient(
    @PrimaryKey
    val uuid: String,

    val addressUuid: String,

    val phoneNumberUuid: String,

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    val ageWhenCreated: Int?,

    val status: PatientStatus,

    val createdAt: Instant,

    val updatedAt: Instant,

    val syncPending: Boolean
) {

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM patient WHERE fullName LIKE '%' || :query || '%'")
    fun search(query: String): Flowable<List<Patient>>

    @Query("SELECT * FROM patient")
    fun allPatients(): Flowable<List<Patient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(patient: Patient)
  }
}
