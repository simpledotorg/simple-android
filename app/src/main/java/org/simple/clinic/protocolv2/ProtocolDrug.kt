package org.simple.clinic.protocolv2

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
import java.util.UUID

@Entity(
    foreignKeys = [
      ForeignKey(entity = Protocol::class,
          parentColumns = ["uuid"],
          childColumns = ["protocolUuid"],
          onDelete = ForeignKey.CASCADE,
          onUpdate = ForeignKey.NO_ACTION
      )],
    indices = [
      Index("protocolUuid",
          unique = false
      )]
)
data class ProtocolDrug(

    @PrimaryKey
    val uuid: UUID,

    val protocolUuid: UUID,

    val name: String,

    val rxNormCode: String?,

    val dosage: String,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(protocolDrugs: List<ProtocolDrug>)

    @Query("SELECT COUNT(uuid) FROM ProtocolDrug")
    fun count(): Flowable<Int>
  }
}
