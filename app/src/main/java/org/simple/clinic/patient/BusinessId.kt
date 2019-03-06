package org.simple.clinic.patient

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
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
import org.simple.clinic.util.SafeEnumTypeAdapter
import org.threeten.bp.Instant
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
data class BusinessId(
    @PrimaryKey val uuid: UUID,
    val patientUuid: UUID,
    val identifier: String,
    val identifierType: IdentifierType,
    val metaVersion: MetaVersion,
    val meta: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?
) {

  sealed class IdentifierType {

    companion object {
      @VisibleForTesting
      fun random() = TypeAdapter.knownMappings.keys.shuffled().first()
    }

    object BpPassport : IdentifierType()

    data class Unknown(val actual: String) : IdentifierType()

    object TypeAdapter: SafeEnumTypeAdapter<IdentifierType>(
        knownMappings = mapOf(
            BpPassport to "simple_bp_passport"
        ),
        unknownStringToEnumConverter = { Unknown(it) },
        unknownEnumToStringConverter = { (it as Unknown).actual }
    )

    class RoomTypeConverter {

      @TypeConverter
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @TypeConverter
      fun fromEnum(enum: IdentifierType?) = TypeAdapter.fromEnum(enum)
    }

    class MoshiTypeAdapter {

      @FromJson
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @ToJson
      fun fromEnum(enum: IdentifierType?) = TypeAdapter.fromEnum(enum)
    }
  }

  sealed class MetaVersion {

    companion object {
      @VisibleForTesting
      fun random() = TypeAdapter.knownMappings.keys.shuffled().first()
    }

    object BpPassportMetaV1 : MetaVersion()

    data class Unknown(val actual: String): MetaVersion()

    object TypeAdapter: SafeEnumTypeAdapter<MetaVersion>(
        knownMappings = mapOf(
            BpPassportMetaV1 to "org.simple.bppassport.meta.v1"
        ),
        unknownStringToEnumConverter = { Unknown(it) },
        unknownEnumToStringConverter = { (it as Unknown).actual }
    )

    class RoomTypeConverter {

      @TypeConverter
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @TypeConverter
      fun fromEnum(enum: MetaVersion?) = TypeAdapter.fromEnum(enum)
    }

    class MoshiTypeAdapter {

      @FromJson
      fun toEnum(value: String?) = TypeAdapter.toEnum(value)

      @ToJson
      fun fromEnum(enum: MetaVersion?) = TypeAdapter.fromEnum(enum)
    }
  }

  @Dao
  interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(businessIds: List<BusinessId>)

    @Query("SELECT COUNT(uuid) FROM BusinessId")
    fun count(): Int
  }
}

