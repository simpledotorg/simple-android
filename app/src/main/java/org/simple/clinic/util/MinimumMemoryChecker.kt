package org.simple.clinic.util

import android.app.ActivityManager
import android.content.Context
import org.simple.clinic.remoteconfig.ConfigReader
import javax.inject.Inject

interface MinimumMemoryChecker {
  fun hasMinimumRequiredMemory(): Boolean
}

class RealMinimumMemoryChecker(
    context: Context,
    private val configReader: ConfigReader
) : MinimumMemoryChecker {

  private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

  override fun hasMinimumRequiredMemory(): Boolean {
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)

    val totalMemoryInGb = memoryInfo.totalMem / 1024.0 / 1024.0 / 1024.0
    return totalMemoryInGb >= configReader.double("minimum_required_memory_in_gb", 3.5)
  }
}
