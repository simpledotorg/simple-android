package org.simple.clinic.patient.businessid

/**
 * This interface describes behaviour for a class that can (de)serialize between [String] and
 * [BusinessIdMeta] instances, given a [BusinessId.MetaVersion].
 **/
interface BusinessIdMetaAdapter {

  fun serialize(meta: BusinessIdMeta, version: BusinessId.MetaVersion): String

  fun deserialize(value: String, version: BusinessId.MetaVersion): BusinessIdMeta
}
