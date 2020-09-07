package org.simple.clinic.util

import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object ThreadPools {

  fun create(
      corePoolSize: Int,
      maxPoolSize: Int,
      threadPrefix: String = "pool[${System.currentTimeMillis() / 1000}]"
  ): Executor {
    return ThreadPoolExecutor(
        corePoolSize, // number of threads to keep alive always
        maxPoolSize, // max number of threads in the pool
        1L, // time to keep non-core threads alive before shutting down
        TimeUnit.SECONDS,
        LinkedBlockingQueue(), // Queue for receiving work
        NamedThreadFactory(threadPrefix)
    )
  }

  private class NamedThreadFactory(
      private val prefix: String
  ) : ThreadFactory {

    private val threadCounter = AtomicInteger(1)

    override fun newThread(job: Runnable): Thread {
      return Thread(job, "$prefix-${threadCounter.getAndIncrement()}")
    }
  }
}
