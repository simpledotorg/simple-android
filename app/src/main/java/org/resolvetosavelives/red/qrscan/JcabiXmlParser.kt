package org.resolvetosavelives.red.qrscan

import com.jcabi.xml.XMLDocument

class JcabiXmlParser(private val xml: String) : XmlParser {

  private var xmlDocument: XMLDocument = XMLDocument(xml)

  override fun readStrings(path: String): List<String> {
    return xmlDocument.xpath(path)
  }

  class Factory : XmlParser.Factory {

    override fun parse(xml: String): XmlParser {
      return JcabiXmlParser(xml)
    }
  }
}
