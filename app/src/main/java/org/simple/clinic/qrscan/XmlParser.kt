package org.simple.clinic.qrscan

interface XmlParser {

  fun hasNode(query: String): Boolean

  /**
   * Apparently, Xml can contain multiple values mapped to the same key.
   * So this function returns a List instead of a single String.
   */
  fun readStrings(path: String): List<String>

  interface Factory {

    @Throws(InvalidXmlException::class)
    fun parse(xml: String): XmlParser
  }
}

class InvalidXmlException(cause: Throwable) : RuntimeException(cause)
