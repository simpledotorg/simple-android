package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.Flowable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.sync.PatientAddressPayload
import org.simple.clinic.storage.DaoWithUpsert
import java.time.Instant
import java.util.UUID

@Entity
@Parcelize
data class PatientAddress(
    @PrimaryKey
    val uuid: UUID,

    val streetAddress: String?,

    val colonyOrVillage: String?,

    val zone: String?,

    val district: String,

    val state: String,

    val country: String?,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) : Parcelable {

  val completeAddress: String
    get() = listOf(
        streetAddress,
        colonyOrVillage,
        district,
        state,
        zone
    ).filterNot { it.isNullOrBlank() }
        .joinToString()

  fun withLocality(
      colonyOrVillage: String,
      district: String,
      state: String,
      zone: String?,
      streetAddress: String?
  ): PatientAddress =
      copy(
          colonyOrVillage = colonyOrVillage,
          district = district,
          state = state,
          zone = zone,
          streetAddress = streetAddress
      )

  fun toPayload(): PatientAddressPayload {
    return PatientAddressPayload(
        uuid = uuid,
        streetAddress = streetAddress,
        colonyOrVillage = colonyOrVillage,
        district = district,
        zone = zone,
        state = state,
        country = country,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt)
  }

  @Dao
  abstract class RoomDao : DaoWithUpsert<PatientAddress>() {

    fun save(address: PatientAddress) {
      save(listOf(address))
    }

    fun save(addresses: List<PatientAddress>) {
      upsert(addresses)
    }

    @Query("SELECT * FROM patientaddress WHERE uuid = :uuid")
    abstract fun getOne(uuid: UUID): PatientAddress?

    @Query("SELECT * FROM patientaddress WHERE uuid = :uuid")
    abstract fun address(uuid: UUID): Flowable<List<PatientAddress>>

    @Query("DELETE FROM patientaddress")
    abstract fun clear()

    @Query("SELECT COUNT(uuid) FROM PatientAddress")
    abstract fun count(): Int

    @Query("SELECT DISTINCT colonyOrVillage FROM PatientAddress WHERE colonyOrVillage IS NOT NULL ORDER BY colonyOrVillage ASC")
    abstract fun getColonyOrVillages(): List<String>
  }
}
