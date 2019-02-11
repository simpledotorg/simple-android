package org.simple.clinic.util

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.arch.core.executor.TaskExecutor
import org.threeten.bp.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("RestrictedApi")
class AppArchTaskExecutorDelegate : TaskExecutor() {

  private val mainThreadHandler = Handler(Looper.getMainLooper())

  private val ioThreadPool: ExecutorService

  init {
    // Keep four threads always alive to handle incoming work
    val corePoolSize = 4
    val maxPoolSize = Int.MAX_VALUE
    val keepAlive = Duration.ofMinutes(1L)
    val threadFactory = object : ThreadFactory {
      private val threadIdGenerator = AtomicInteger(0)

      override fun newThread(runnable: Runnable): Thread {
        val thread = Thread(runnable)
        thread.name = "arch_disk_io_${threadIdGenerator.getAndIncrement()}"
        return thread
      }
    }

    ioThreadPool = ThreadPoolExecutor(
        corePoolSize,
        maxPoolSize,
        keepAlive.seconds,
        TimeUnit.SECONDS,
        SynchronousQueue(),
        threadFactory
    )
  }

  override fun executeOnDiskIO(runnable: Runnable) {
    ioThreadPool.execute(runnable)
  }

  override fun isMainThread(): Boolean {
    return Looper.getMainLooper().thread === Thread.currentThread()
  }

  override fun postToMainThread(runnable: Runnable) {
    mainThreadHandler.post(runnable)
  }
}
