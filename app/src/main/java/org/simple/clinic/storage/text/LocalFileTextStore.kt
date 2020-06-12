package org.simple.clinic.storage.text

import io.reactivex.Observable
import org.simple.clinic.storage.files.FileStorage
import org.simple.clinic.storage.files.GetFileResult
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toOptional
import javax.inject.Inject

/*
* Temporarily added to ease migration. This class will be removed later.
**/
class LocalFileTextStore @Inject constructor(
    private val fileStorage: FileStorage,
    private val keysToFilePath: Map<String, String>
) : TextStore {

  override fun get(key: String): String? {
    val filePathForKey = keysToFilePath.getValue(key)

    return when (val result = fileStorage.getFile(filePathForKey)) {
      is GetFileResult.Success -> result.file.readText()
      is GetFileResult.NotAFile, is GetFileResult.Failure -> null
    }
  }

  override fun getAsStream(key: String): Observable<Optional<String>> {
    /*
    * Ideally, we would like to create a "true" observable stream here.
    *
    * However, this is going to get replaced with a Room database table
    * in an upcoming commit which should give us the behaviour. This
    * class will be deleted then, so there is no point in implementing
    * all of that.
    **/
    return Observable.fromCallable { get(key).toOptional() }
  }

  override fun put(key: String, value: String?) {
    val filePathForKey = keysToFilePath.getValue(key)

    val file = (fileStorage.getFile(filePathForKey) as GetFileResult.Success).file

    fileStorage.writeToFile(file, value ?: "")
  }

  override fun delete(key: String) {
    val filePathForKey = keysToFilePath.getValue(key)

    val file = (fileStorage.getFile(filePathForKey) as GetFileResult.Success).file

    fileStorage.delete(file)
  }
}
