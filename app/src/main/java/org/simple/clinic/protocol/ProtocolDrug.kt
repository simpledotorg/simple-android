package org.simple.clinic.protocol

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
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

    val deletedAt: Instant?,

    val order: Int
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(protocolDrugs: List<ProtocolDrug>)

    @Query("SELECT COUNT(uuid) FROM ProtocolDrug")
    fun count(): Flowable<Int>

    @Query("SELECT * FROM ProtocolDrug WHERE protocolUuid = :protocolUuid ORDER BY `order`")
    fun drugsForProtocolUuid(protocolUuid: UUID): Flowable<List<ProtocolDrug>>

    @Query("SELECT dosage FROM ProtocolDrug WHERE protocolUuid = :protocolUuid AND name = :drugName")
    fun dosagesForDrug(drugName: String, protocolUuid: UUID): Flowable<List<String>>
  }
}
