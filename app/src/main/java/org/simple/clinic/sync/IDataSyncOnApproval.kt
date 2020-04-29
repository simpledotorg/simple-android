package org.simple.clinic.sync

import io.reactivex.disposables.Disposable
import javax.annotation.CheckReturnValue

interface IDataSyncOnApproval {
  @CheckReturnValue
  fun sync(): Disposable
}
