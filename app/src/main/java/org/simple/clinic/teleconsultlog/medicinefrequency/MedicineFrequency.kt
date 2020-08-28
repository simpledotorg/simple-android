package org.simple.clinic.teleconsultlog.medicinefrequency

import android.os.Parcelable
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.util.room.SafeEnumTypeAdapter

sealed class MedicineFrequency : Parcelable {

  @Parcelize
  object OD : MedicineFrequency()

  @Parcelize
  object BD : MedicineFrequency()

  @Parcelize
  object TDS : MedicineFrequency()

  @Parcelize
  object QDS : MedicineFrequency()

  @Parcelize
  data class Unknown(val actualValue: String) : MedicineFrequency()

  object TypeAdapter : SafeEnumTypeAdapter<MedicineFrequency>(
      knownMappings = mapOf(
          OD to "od",
          BD to "bd",
          TDS to "tds",
          QDS to "qds"
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
