package org.simple.clinic.remoteconfig

import com.google.android.gms.tasks.Task
import io.reactivex.Completable
import io.reactivex.CompletableEmitter

class CompletableEmitterGmsTaskHandler<R> {

  private var unsubscribed: Boolean = false

  fun bind(
      emitter: CompletableEmitter,
      task: Task<R>,
      onTaskUnsuccessful: (Task<R>) -> Unit = {}
  ) {
    emitter.setCancellable { unsubscribed = true }

    task
        .addOnCompleteListener {
          if (task.isSuccessful) {
            emitter.onComplete()
          } else {
            onTaskUnsuccessful.invoke(task)
          }
        }
        .addOnFailureListener {
          if (!unsubscribed) {
            emitter.onError(it)
          }
        }
  }
}

fun <R> Task<R>.toCompletable(onTaskUnsuccessful: (Task<R>) -> Unit = {}): Completable {
  return Completable.create { emitter ->
    val handler = CompletableEmitterGmsTaskHandler<R>()
    handler.bind(emitter, this, onTaskUnsuccessful)
  }
}
