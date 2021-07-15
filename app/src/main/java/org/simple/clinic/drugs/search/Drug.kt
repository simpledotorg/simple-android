package org.simple.clinic.drugs.search

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Observable
import org.simple.clinic.storage.Timestamps
import java.util.UUID

@Entity
data class Drug(
    @PrimaryKey
    val id: UUID,

    val name: String,

    val category: DrugCategory?,

    val frequency: DrugFrequency?,

    val composition: String?,

    val dosage: String?,

    val rxNormCode: String?,

    val protocol: Answer,

    val common: Answer,

    @Embedded
    val timestamps: Timestamps
) {

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(drugs: List<Drug>)

    @Query("SELECT * FROM Drug")
    fun getAll(): List<Drug>

    @Query("SELECT COUNT(uuid) FROM Protocol")
    fun count(): Observable<Int>

    @Query("""
      SELECT drugs.* FROM Drug drugs
      LEFT JOIN ProtocolDrug PD ON PD.protocolUuid == :protocolId AND PD.rxNormCode == drugs.rxNormCode
      WHERE drugs.name LIKE '%' || :query || '%' COLLATE NOCASE AND PD.uuid IS NULL
      GROUP BY drugs.id
      ORDER BY drugs.name COLLATE NOCASE, drugs.dosage ASC
    """)
    fun searchForNonProtocolDrugs(query: String, protocolId: UUID?): PagingSource<Int, Drug>
  }
}
