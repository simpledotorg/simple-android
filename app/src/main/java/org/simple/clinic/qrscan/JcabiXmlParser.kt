package org.simple.clinic.qrscan

import com.jcabi.xml.XMLDocument

class JcabiXmlParser(xml: String) : XmlParser {

  private var xmlDocument: XMLDocument = XMLDocument(xml)

  override fun readStrings(path: String): List<String> {
    return xmlDocument.xpath(path)
  }

  override fun hasNode(query: String): Boolean {
    return xmlDocument.nodes(query).isNotEmpty()
  }

  class Factory : XmlParser.Factory {

    @Throws(InvalidXmlException::class)
    override fun parse(xml: String): XmlParser {
      try {
        return JcabiXmlParser(xml)

      } catch (e: IllegalArgumentException) {
        if (e.message?.contains("Doesn't look like XML", ignoreCase = false) == true) {
          throw InvalidXmlException(e)

        } else {
          throw e
        }
      }
    }
  }
}
