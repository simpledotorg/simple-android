package org.simple.clinic.help

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.di.AppScope
import org.simple.clinic.storage.files.DeleteFileResult
import org.simple.clinic.storage.files.FileStorage
import org.simple.clinic.storage.files.GetFileResult
import org.simple.clinic.storage.files.WriteFileResult
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.ofType
import org.simple.clinic.util.toOptional
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@AppScope
class HelpRepository @Inject constructor(
    private val fileStorage: FileStorage,
    @Named("help_file_path") private val helpFilePath: String
) {
  private val fileChangedSubject = PublishSubject.create<Optional<File>>()

  fun helpFile(): Observable<Optional<File>> {
    val initialFile = Observable
        .fromCallable { fileStorage.getFile(helpFilePath) }
        .map { result ->
          if (result is GetFileResult.Success && result.file.length() > 0L) {
            Just(result.file)

          } else {
            None
          }
        }

    return fileChangedSubject.mergeWith(initialFile)
  }

  fun updateHelp(helpContent: String): Completable {
    return Single.fromCallable { fileStorage.getFile(helpFilePath) }
        .ofType<GetFileResult.Success>()
        .map { fileStorage.writeToFile(it.file, helpContent) }
        .ofType(WriteFileResult.Success::class.java)
        .doOnSuccess { fileChangedSubject.onNext(it.file.toOptional()) }
        .ignoreElement()
  }

  fun deleteHelpFile(): Single<DeleteFileResult> {
    return Single.fromCallable { fileStorage.getFile(helpFilePath) }
        .ofType<GetFileResult.Success>()
        .map { fileStorage.delete(it.file) }
        .doOnSuccess {
          if (it is DeleteFileResult.Success) fileChangedSubject.onNext(None)
        }
        .toSingle(DeleteFileResult.Success)
  }
}
