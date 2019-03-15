package org.simple.clinic.patient.businessid

import com.squareup.moshi.JsonAdapter

class MoshiBusinessIdMetaAdapter(
    private val adapters: Map<BusinessId.MetaVersion, JsonAdapter<BusinessIdMeta>>
) : BusinessIdMetaAdapter {

  override fun serialize(meta: BusinessIdMeta, version: BusinessId.MetaVersion): String {
    return jsonAdapterForMetaVersion(version).toJson(meta)
  }

  override fun deserialize(value: String, version: BusinessId.MetaVersion): BusinessIdMeta {
    return jsonAdapterForMetaVersion(version).fromJson(value)!!
  }

  private fun jsonAdapterForMetaVersion(version: BusinessId.MetaVersion) = adapters.getValue(version)
}
