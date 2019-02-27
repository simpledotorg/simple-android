package org.simple.clinic.patient

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
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

  enum class IdentifierType {
    BP_PASSPORT,
    UNKNOWN;

    class RoomTypeConverter {

      @TypeConverter
      fun fromStringValue(stringValue: String) = try {
        IdentifierType.valueOf(stringValue)
      } catch (e: IllegalArgumentException) {
        UNKNOWN
      }

      @TypeConverter
      fun toStringValue(identifierType: IdentifierType) = identifierType.name
    }
  }

  enum class MetaVersion {
    BP_PASSPORT_META_V1,
    UNKNOWN;

    class RoomTypeConverter {

      @TypeConverter
      fun fromStringValue(stringValue: String) = try {
        MetaVersion.valueOf(stringValue)
      } catch (e: IllegalArgumentException) {
        UNKNOWN
      }

      @TypeConverter
      fun toStringValue(metaVersion: MetaVersion) = metaVersion.name
    }
  }
}
