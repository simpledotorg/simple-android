package org.simple.clinic.remoteconfig

import com.google.android.gms.tasks.Task
import io.reactivex.CompletableEmitter

class CompletableEmitterGmsTaskHandler<R> {

  private var unsubscribed: Boolean = false

  fun bind(
      emitter: CompletableEmitter,
      task: Task<R>,
      onTaskUnsuccessful: ((Task<R>) -> Unit)? = null
  ) {
    emitter.setCancellable { unsubscribed = true }

    task
        .addOnCompleteListener {
          if (task.isSuccessful) {
            emitter.onComplete()
          } else {
            onTaskUnsuccessful?.invoke(task)
          }
        }
        .addOnFailureListener {
          if (!unsubscribed) {
            emitter.onError(it)
          }
        }
  }
}
