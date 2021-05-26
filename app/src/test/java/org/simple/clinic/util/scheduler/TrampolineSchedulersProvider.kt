package org.simple.clinic.util.scheduler

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TrampolineSchedulersProvider
@Deprecated(
    message = "Use TestSchedulersProvider instead",
    replaceWith = ReplaceWith(
        expression = "TestSchedulersProvider.trampoline()",
        imports = ["org.simple.clinic.util.scheduler.TestSchedulersProvider"]
    )
)
@Inject constructor() : SchedulersProvider {
  override fun io(): Scheduler = Schedulers.trampoline()
  override fun computation(): Scheduler = Schedulers.trampoline()
  override fun ui(): Scheduler = Schedulers.trampoline()
}
