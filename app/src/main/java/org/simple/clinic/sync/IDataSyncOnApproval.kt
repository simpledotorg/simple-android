package org.simple.clinic.sync

import io.reactivex.annotations.CheckReturnValue
import io.reactivex.disposables.Disposable

interface IDataSyncOnApproval {
  @CheckReturnValue
  fun sync(): Disposable
}
