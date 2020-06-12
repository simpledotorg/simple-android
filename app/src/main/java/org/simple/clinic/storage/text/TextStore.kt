package org.simple.clinic.storage.text

import io.reactivex.Observable
import org.simple.clinic.util.Optional

interface TextStore {

  fun get(key: String): String?

  fun getAsStream(key: String): Observable<Optional<String>>

  fun put(key: String, value: String?)

  fun delete(key: String)
}
