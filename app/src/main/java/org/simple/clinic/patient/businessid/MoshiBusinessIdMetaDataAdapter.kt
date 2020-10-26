package org.simple.clinic.patient.businessid

import com.squareup.moshi.JsonAdapter

class MoshiBusinessIdMetaDataAdapter(
    private val adapter: JsonAdapter<BusinessIdMetaData>
) : BusinessIdMetaDataAdapter {

  override fun serialize(metaData: BusinessIdMetaData, metaDataVersion: BusinessId.MetaDataVersion): String {
    return adapter.toJson(metaData)
  }

  override fun deserialize(value: String, metaDataVersion: BusinessId.MetaDataVersion): BusinessIdMetaData {
    return adapter.fromJson(value)!!
  }
}
