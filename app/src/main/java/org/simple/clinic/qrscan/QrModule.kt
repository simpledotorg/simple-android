package org.simple.clinic.qrscan

import dagger.Module
import dagger.Provides

@Module
class QrModule {

  @Provides
  fun xmlParserFactory(): XmlParser.Factory {
    return JcabiXmlParser.Factory()
  }
}
