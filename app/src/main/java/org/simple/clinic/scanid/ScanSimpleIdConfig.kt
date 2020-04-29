package org.simple.clinic.scanid

import org.simple.clinic.remoteconfig.ConfigReader
import javax.inject.Inject

data class ScanSimpleIdConfig(val useNewQrScanner: Boolean) {

  @Inject
  constructor(configReader: ConfigReader) : this(
      useNewQrScanner = configReader.boolean("use_new_qr_scanner", true)
  )
}
