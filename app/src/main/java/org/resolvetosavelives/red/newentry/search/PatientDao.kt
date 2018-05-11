package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface PatientDao {

  @Query("SELECT * FROM patient WHERE full_name LIKE '%' || :query || '%' OR mobile_number LIKE '%' || :query || '%'")
  fun search(query: String): Flowable<List<Patient>>

  @Query("SELECT * FROM patient")
  fun allPatients(): Flowable<List<Patient>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun save(patient: Patient)
}
