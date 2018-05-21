package org.resolvetosavelives.red.qrscan

import dagger.Module
import dagger.Provides

@Module
class QrDaggerModule {

  @Provides
  fun xmlParserFactory(): XmlParser.Factory {
    return JcabiXmlParser.Factory()
  }
}
