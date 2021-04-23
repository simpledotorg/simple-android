package org.simple.clinic.patient.businessid

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.businessid.Identifier.IdentifierType
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.EthiopiaMedicalRecordNumber
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.patient.sync.BusinessIdPayload
import org.simple.clinic.util.Optional
import org.simple.clinic.util.room.SafeEnumTypeAdapter
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "BusinessId",
    foreignKeys = [
      ForeignKey(
          entity = Patient::class,
          parentColumns = ["uuid"],
          childColumns = ["patientUuid"],
          onDelete = ForeignKey.CASCADE
      )
    ],
    indices = [
      Index("patientUuid"),
      Index("identifier")
    ]
)
@Parcelize
data class BusinessId(
    @PrimaryKey val uuid: UUID,

    val patientUuid: UUID,

    @Embedded val identifier: Identifier,

    @ColumnInfo(name = "metaVersion")
    val metaDataVersion: MetaDataVersion,

    @ColumnInfo(name = "meta")
    val metaData: String,

    val createdAt: Instant,

    val updatedAt: Instant,

    val deletedAt: Instant?
) : Parcelable {

  fun toPayload(): BusinessIdPayload {
    return BusinessIdPayload(
        uuid = uuid,
        identifier = identifier.value,
        identifierType = identifier.type,
        metaDataVersion = metaDataVersion,
        metaData = metaData,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
  }

  fun updateIdentifierValue(newValue: String): BusinessId =
      copy(identifier = identifier.copy(value = newValue))

  sealed class MetaDataVersion : Parcelable {

    companion object {
      @VisibleForTesting(otherwise = VisibleForTesting.NONE)
      fun random() = TypeAdapter.knownMappings.keys.shuffled().first()

      @VisibleForTesting(otherwise = VisibleForTesting.NONE)
      fun values() = TypeAdapter.knownMappings.keys

      fun forIdentifierType(
          type: IdentifierType
      ): Optional<MetaDataVersion> {
        val metaDataVersion = when (type) {
          BpPassport -> BpPassportMetaDataV1
          BangladeshNationalId -> BangladeshNationalIdMetaDataV1
          EthiopiaMedicalRecordNumber -> MedicalRecordNumberMetaDataV1
          IndiaNationalHealthId -> IndiaNationalHealthIdMetaDataV1
          is IdentifierType.Unknown -> null
        }

        return Optional.ofNullable(metaDataVersion)
      }
    }

    @Parcelize
    object BpPassportMetaDataV1 : MetaDataVersion()

    @Parcelize
    object BangladeshNationalIdMetaDataV1 : MetaDataVersion()

    @Parcelize
    object MedicalRecordNumberMetaDataV1 : MetaDataVersion()

    @Parcelize
    object IndiaNationalHealthIdMetaDataV1 : MetaDataVersion()

    @Parcelize
    data class Unknown(val actual: String) : MetaDataVersion()

    object TypeAdapter : SafeEnumTypeAdapter<MetaDataVersion>(
        knownMappings = mapOf(
            BpPassportMetaDataV1 to "org.simple.bppassport.meta.v1",
            BangladeshNationalIdMetaDataV1 to "org.simple.bangladesh_national_id.meta.v1",
            MedicalRecordNumberMetaDataV1 to "org.simple.ethiopia_medical_record.meta.v1"
        ),
        unknownStringToEnumConverter = { Unknown(it) },
        unknownEnumToStringConverter = { (it as Unknown).actual }
    )

    class RoomTypeConverter {

      @TypeConverter
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @TypeConverter
      fun fromEnum(enum: MetaDataVersion?) = TypeAdapter.fromEnum(enum)
    }

    class MoshiTypeAdapter {

      @FromJson
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @ToJson
      fun fromEnum(enum: MetaDataVersion?) = TypeAdapter.fromEnum(enum)
    }
  }

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(businessIds: List<BusinessId>)

    @Query("SELECT COUNT(uuid) FROM BusinessId")
    fun count(): Int

    @Query("""
      SELECT * FROM BusinessId
      WHERE patientUuid = :patientUuid AND identifierType = :identifierType AND deletedAt IS NULL
      ORDER BY createdAt DESC LIMIT 1
      """)
    fun latestForPatientByType(patientUuid: UUID, identifierType: IdentifierType): Flowable<List<BusinessId>>

    @Query("""
      SELECT * FROM BusinessId
      WHERE patientUuid = :patientUuid AND identifierType = :identifierType AND deletedAt IS NULL
      ORDER BY createdAt DESC LIMIT 1
      """)
    fun latestForPatientByTypeImmediate(patientUuid: UUID, identifierType: IdentifierType): List<BusinessId>

    @Query("SELECT * FROM BusinessId WHERE identifierType = :identifierType")
    fun allBusinessIdsWithType(identifierType: IdentifierType): Single<List<BusinessId>>

    @Query("SELECT * FROM BusinessId WHERE uuid = :uuid")
    fun get(uuid: UUID): BusinessId?
  }
}

