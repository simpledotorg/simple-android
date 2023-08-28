package org.simple.clinic.util

import android.app.ActivityManager
import android.content.Context

interface MinimumMemoryChecker {
  fun hasMinimumRequiredMemory(): Boolean
}

class RealMinimumMemoryChecker(context: Context) : MinimumMemoryChecker {

  companion object {
    private const val MIN_REQUIRED_MEMORY = 5.5 // GB
  }

  private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

  override fun hasMinimumRequiredMemory(): Boolean {
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)

    val totalMemoryInGb = memoryInfo.totalMem / 1024.0 / 1024.0 / 1024.0
    return totalMemoryInGb >= MIN_REQUIRED_MEMORY
  }
}
