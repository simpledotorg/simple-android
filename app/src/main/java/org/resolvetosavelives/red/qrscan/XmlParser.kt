package org.resolvetosavelives.red.qrscan

interface XmlParser {
  /**
   * Apparently, Xml can contain multiple values mapped to the same key.
   * So this function returns a List instead of a single String.
   */
  fun readStrings(path: String): List<String>

  interface Factory {
    fun parse(xml: String): XmlParser
  }
}
