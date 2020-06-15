package org.simple.clinic.reports

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
import java.util.function.Function
import javax.inject.Inject
import javax.inject.Named

@AppScope
class ReportsRepository @Inject constructor(
    private val fileStorage: FileStorage,
    @Named("reports_file_path") private val reportsFilePath: String
) {
  private val fileChangedSubject = PublishSubject.create<Optional<File>>()

  fun reportsFile(): Observable<Optional<File>> {
    val initialFile = Observable
        .fromCallable { fileStorage.getFile(reportsFilePath) }
        .map { result ->
          if (result is GetFileResult.Success && result.file.length() > 0L) {
            Just(result.file)
          } else {
            None<File>()
          }
        }

    return fileChangedSubject.mergeWith(initialFile)
  }

  fun reportsContentText(): Observable<Optional<String>> {
    val initialFile = Observable
        .fromCallable { fileStorage.getFile(reportsFilePath) }
        .map { result ->
          if (result is GetFileResult.Success && result.file.length() > 0L) {
            Optional.of(result.file)
          } else {
            Optional.empty()
          }
        }

    return initialFile
        .mergeWith(fileChangedSubject)
        .map { fileOptional -> fileOptional.map(Function<File, String> { it.readText() }) }
  }

  fun updateReports(reportContent: String): Completable =
      Single.fromCallable { fileStorage.getFile(reportsFilePath) }
          .ofType<GetFileResult.Success>()
          .map { fileStorage.writeToFile(it.file, reportContent) }
          .ofType(WriteFileResult.Success::class.java)
          .doOnSuccess { fileChangedSubject.onNext(it.file.toOptional()) }
          .ignoreElement()

  fun deleteReportsFile(): Single<DeleteFileResult> =
      Single.fromCallable { fileStorage.getFile(reportsFilePath) }
          .ofType<GetFileResult.Success>()
          .map { fileStorage.delete(it.file) }
          .doOnSuccess {
            if (it is DeleteFileResult.Success) fileChangedSubject.onNext(None())
          }
          .toSingle(DeleteFileResult.Success)
}
