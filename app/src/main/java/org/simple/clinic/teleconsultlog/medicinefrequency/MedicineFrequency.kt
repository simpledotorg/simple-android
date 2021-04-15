package org.simple.clinic.teleconsultlog.medicinefrequency

import android.os.Parcelable
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class MedicineFrequency : Parcelable {

  @Parcelize
  object OD : MedicineFrequency() {
    override fun toString(): String {
      return "OD"
    }
  }

  @Parcelize
  object BD : MedicineFrequency() {
    override fun toString(): String {
      return "BD"
    }
  }

  @Parcelize
  object TDS : MedicineFrequency() {
    override fun toString(): String {
      return "TDS"
    }
  }

  @Parcelize
  object QDS : MedicineFrequency() {
    override fun toString(): String {
      return "QDS"
    }
  }

  @Parcelize
  data class Unknown(val actualValue: String) : MedicineFrequency()

  object TypeAdapter : SafeEnumTypeAdapter<MedicineFrequency>(
      knownMappings = mapOf(
          OD to "OD",
          BD to "BD",
          TDS to "TDS",
          QDS to "QDS"
      ),
      unknownStringToEnumConverter = { Unknown(it) },
      unknownEnumToStringConverter = { (it as Unknown).actualValue }
  )

  class RoomTypeConverter {
    @TypeConverter
    fun toEnum(value: String?): MedicineFrequency? = TypeAdapter.toEnum(value)

    @TypeConverter
    fun fromEnum(medicineFrequency: MedicineFrequency?): String? = TypeAdapter.fromEnum(medicineFrequency)
  }

  class MoshiTypeConverter {
    @FromJson
    fun toEnum(value: String?): MedicineFrequency? = TypeAdapter.toEnum(value)

    @ToJson
    fun fromEnum(medicineFrequency: MedicineFrequency?): String? = TypeAdapter.fromEnum(medicineFrequency)
  }
}
