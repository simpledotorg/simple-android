package org.simple.clinic.patient.businessid

import com.squareup.moshi.JsonAdapter

class MoshiBusinessIdMetaDataAdapter(
    private val adapters: Map<BusinessId.MetaDataVersion, JsonAdapter<BusinessIdMetaData>>
) : BusinessIdMetaDataAdapter {

  override fun serialize(metaData: BusinessIdMetaData, metaDataVersion: BusinessId.MetaDataVersion): String {
    return jsonAdapterForMetaVersion(metaDataVersion).toJson(metaData)
  }

  override fun deserialize(value: String, metaDataVersion: BusinessId.MetaDataVersion): BusinessIdMetaData {
    return jsonAdapterForMetaVersion(metaDataVersion).fromJson(value)!!
  }

  private fun jsonAdapterForMetaVersion(metaDataVersion: BusinessId.MetaDataVersion) = adapters.getValue(metaDataVersion)
}
