package org.simple.clinic.questionnaire.component

import android.os.Parcelable
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
open class BaseComponentData : Parcelable {

  @ProvidedTypeConverter
  class RoomTypeConverter(
      private val moshi: Moshi
  ) {
    @TypeConverter
    fun fromComponentData(layout: BaseComponentData): String {
      return moshi.adapter(BaseComponentData::class.java).toJson(layout)
    }

    @TypeConverter
    fun toComponentData(layoutString: String): BaseComponentData? {
      return moshi.adapter(BaseComponentData::class.java).fromJson(layoutString)
    }
  }
}



