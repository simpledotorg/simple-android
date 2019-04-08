package org.simple.clinic.patient.businessid

/**
 * This interface describes behaviour for a class that can (de)serialize between [String] and
 * [BusinessIdMetaData] instances, given a [BusinessId.MetaDataVersion].
 **/
interface BusinessIdMetaDataAdapter {

  fun serialize(metaData: BusinessIdMetaData, metaDataVersion: BusinessId.MetaDataVersion): String

  fun deserialize(value: String, metaDataVersion: BusinessId.MetaDataVersion): BusinessIdMetaData
}
