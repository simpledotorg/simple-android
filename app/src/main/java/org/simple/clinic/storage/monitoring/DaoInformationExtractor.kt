package org.simple.clinic.storage.monitoring

import javax.inject.Inject

class DaoInformationExtractor @Inject constructor(
    private val dbMethodLookup: DbMethodLookup
) {

  fun findDaoMethodInCurrentCallStack(): DaoMethodInformation? {
    val throwable = Throwable()

    return throwable
        .stackTrace
        .firstOrNull { it.className.startsWith("org.simple.clinic.") && it.fileName.endsWith("_Impl.java") }
        ?.let { stackTraceElement ->
          val className = stackTraceElement.fileName.removeSuffix(".java")
          val lineNumber = stackTraceElement.lineNumber
          val methodName = dbMethodLookup.find(className, lineNumber)

          if (methodName != null) DaoMethodInformation(className, methodName) else null
        }
  }

  data class DaoMethodInformation(
      val daoName: String,
      val methodName: String
  )
}
