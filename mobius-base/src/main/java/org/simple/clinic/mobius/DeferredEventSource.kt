package org.simple.clinic.mobius

import com.spotify.mobius.EventSource
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class was repurposed from https://github.com/spotify/mobius-android-sample/blob/5778c008a8b7acb88adf5c2c652bed53de66fd80/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/view/DeferredEventSource.java
 **/
class DeferredEventSource<E> : EventSource<E> {

  private val events: BlockingQueue<E> = LinkedBlockingQueue()

  override fun subscribe(eventConsumer: Consumer<E>): Disposable {
    val run = AtomicBoolean(true)

    val thread = Thread {
      while (run.get()) {
        try {
          val event = events.take()

          if (run.get()) {
            eventConsumer.accept(event)
          }

        } catch (e: InterruptedException) {
          // Nothing to do here
        }
      }
    }

    thread.start()

    return Disposable {
      run.set(false)
      thread.interrupt()
    }
  }

  @Synchronized
  fun notify(event: E) {
    events.offer(event)
  }
}
