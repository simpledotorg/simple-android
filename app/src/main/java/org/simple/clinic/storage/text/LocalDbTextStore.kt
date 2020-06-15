package org.simple.clinic.storage.text

import io.reactivex.Observable
import org.simple.clinic.util.Optional
import javax.inject.Inject

class LocalDbTextStore @Inject constructor(
    private val dao: TextRecord.RoomDao
) : TextStore {

  override fun get(key: String): String? {
    return dao.get(key)?.text
  }

  override fun getAsStream(key: String): Observable<Optional<String>> {
    return dao
        .changes(key)
        .map { records ->
          if (records.isEmpty())
            Optional.empty()
          else
            Optional.ofNullable(records.first().text)
        }
  }

  override fun put(key: String, value: String?) {
    dao.save(TextRecord(key, value))
  }

  override fun delete(key: String) {
    dao.delete(key)
  }
}
