package org.simple.clinic.returnscore

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.storage.Timestamps
import java.util.UUID

@Entity(tableName = "ReturnScore",
    indices = [
      Index("patientUuid")
    ])
@Parcelize
data class ReturnScore(
    @PrimaryKey
    val uuid: UUID,

    val patientUuid: UUID,

    val scoreType: ScoreType,

    val scoreValue: Float,

    @Embedded
    val timestamps: Timestamps,
) : Parcelable {

  @Dao
  interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(returnScores: List<ReturnScore>)

    @Query("SELECT * FROM ReturnScore WHERE deletedAt IS NULL")
    fun getAll(): Flowable<List<ReturnScore>>

    @Query("SELECT * FROM ReturnScore WHERE deletedAt IS NULL")
    fun getAllImmediate(): List<ReturnScore>

    @Query("SELECT * FROM ReturnScore WHERE scoreType == :type AND deletedAt IS NULL LIMIT 1")
    fun getByScoreType(type: ScoreType): Flowable<List<ReturnScore>>

    @Query("SELECT COUNT(uuid) FROM ReturnScore")
    fun count(): Observable<Int>

    @Query("DELETE FROM returnscore")
    fun clear(): Int

    @Query("""
      DELETE FROM ReturnScore
      WHERE deletedAt IS NOT NULL
    """)
    fun purgeDeleted(): Int
  }
}
