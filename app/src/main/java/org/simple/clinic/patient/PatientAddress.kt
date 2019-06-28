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
import org.threeten.bp.Instant
import java.util.UUID

@Entity
@Parcelize
data class PatientAddress(
    @PrimaryKey
    val uuid: UUID,

    val colonyOrVillage: String?,

    val district: String,

    val state: String,

    // TODO: Don't use India as the country for everyone!
    val country: String? = "India",

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) : Parcelable {

  fun toPayload(): PatientAddressPayload {
    return PatientAddressPayload(
        uuid = uuid,
        colonyOrVillage = colonyOrVillage,
        district = district,
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
  }
}
