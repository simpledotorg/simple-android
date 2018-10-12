package org.simple.clinic.sync

import io.reactivex.Completable

interface ModelSync {
  fun sync(): Completable
  fun push(): Completable
  fun pull(): Completable
}
