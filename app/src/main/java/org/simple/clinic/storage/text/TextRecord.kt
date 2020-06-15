package org.simple.clinic.storage.text

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Observable

@Entity(tableName = "TextRecords")
data class TextRecord(

    @PrimaryKey
    val id: String,

    val text: String?
) {

  @Dao
  interface RoomDao {

    @Query("SELECT * FROM TextRecords WHERE id = :id")
    fun get(id: String): TextRecord?

    @Query("SELECT * FROM TextRecords WHERE id = :id")
    fun changes(id: String): Observable<List<TextRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(record: TextRecord)

    @Query("DELETE FROM TextRecords WHERE id = :id")
    fun delete(id: String)
  }
}
