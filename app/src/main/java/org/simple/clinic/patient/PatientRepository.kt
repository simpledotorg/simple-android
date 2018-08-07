package org.simple.clinic.patient

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.newentry.DateOfBirthFormatValidator
import org.simple.clinic.patient.SyncStatus.DONE
import org.simple.clinic.patient.SyncStatus.PENDING
import org.simple.clinic.patient.sync.PatientPayload
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Arrays
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@AppScope
class PatientRepository @Inject constructor(
    private val database: AppDatabase,
    private val dobValidator: DateOfBirthFormatValidator
) {

  companion object {
    val dateOfTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy", Locale.ENGLISH)
  }

  private val ageFuzziness: Int = 3

  private var ongoingPatientEntry: OngoingPatientEntry = OngoingPatientEntry()

  fun searchPatientsAndPhoneNumbers(
      query: String?,
      includeFuzzyNameSearch: Boolean = true
  ): Observable<List<PatientSearchResult>> {
    if (query.isNullOrEmpty()) {
      return database.patientSearchDao()
          .recentlyUpdated100Records()
          .toObservable()
    }

    val actualQuery = nameToSearchableForm(query!!)

    return if (actualQuery.all { it.isDigit() } || includeFuzzyNameSearch.not()) {
      database.patientSearchDao()
          .search(actualQuery)
          .toObservable()
    } else {
      val fuzzySearch = database.fuzzyPatientSearchDao()
          .searchForPatientsWithNameLike(actualQuery)
          .toObservable()

      database.patientSearchDao()
          .search(actualQuery)
          .toObservable()
          .zipWith(fuzzySearch)
          .map { (results, fuzzyResults) -> (fuzzyResults + results).distinctBy { it.uuid } }
    }
  }

  fun searchPatientsAndPhoneNumbers(
      query: String?,
      assumedAge: Int,
      includeFuzzyNameSearch: Boolean = true
  ): Observable<List<PatientSearchResult>> {
    val ageUpperBound = assumedAge + ageFuzziness
    val ageLowerBound = assumedAge - ageFuzziness

    val dateOfBirthUpperBound = LocalDate.now(ZoneOffset.UTC).minusYears(ageUpperBound.toLong()).toString()
    val dateOfBirthLowerBound = LocalDate.now(ZoneOffset.UTC).minusYears(ageLowerBound.toLong()).toString()

    if (query.isNullOrEmpty()) {
      return database.patientSearchDao()
          .search(dateOfBirthUpperBound, dateOfBirthLowerBound)
          .toObservable()
    }

    val actualQuery = nameToSearchableForm(query!!)

    return if (actualQuery.all { it.isDigit() } || includeFuzzyNameSearch.not()) {
      database.patientSearchDao()
          .search(actualQuery, dateOfBirthUpperBound, dateOfBirthLowerBound)
          .toObservable()
    } else {
      val fuzzySearch = database.fuzzyPatientSearchDao()
          .searchForPatientsWithNameLikeAndAgeWithin(actualQuery, dateOfBirthUpperBound, dateOfBirthLowerBound)
          .toObservable()

      database.patientSearchDao()
          .search(actualQuery, dateOfBirthUpperBound, dateOfBirthLowerBound)
          .toObservable()
          .zipWith(fuzzySearch)
          .map { (results, fuzzyResults) -> (fuzzyResults + results).distinctBy { it.uuid } }
    }
  }

  fun patientCount(): Single<Int> {
    return database.patientDao()
        .patientCount()
        .firstOrError()
  }

  fun patientsWithSyncStatus(status: SyncStatus): Single<List<PatientSearchResult>> {
    return database.patientSearchDao()
        .withSyncStatus(status)
        .firstOrError()
  }

  fun updatePatientsSyncStatus(oldStatus: SyncStatus, newStatus: SyncStatus): Completable {
    return Completable.fromAction {
      database.patientDao().updateSyncStatus(oldStatus = oldStatus, newStatus = newStatus)
    }
  }

  fun updatePatientsSyncStatus(patientUuids: List<UUID>, newStatus: SyncStatus): Completable {
    if (patientUuids.isEmpty()) {
      throw AssertionError()
    }
    return Completable.fromAction {
      database.patientDao().updateSyncStatus(uuids = patientUuids, newStatus = newStatus)
    }
  }

  private fun savePatient(patient: Patient): Completable {
    return Completable.fromAction { database.patientDao().save(patient) }
        .andThen(database.fuzzyPatientSearchDao().updateFuzzySearchTableForPatients(listOf(patient.uuid)))
  }

  private fun savePatients(patients: List<Patient>): Completable {
    return Completable.fromAction { database.patientDao().save(patients) }
        .andThen(database.fuzzyPatientSearchDao().updateFuzzySearchTableForPatients(patients.map { it.uuid }.distinct()))
  }

  fun patient(uuid: UUID): Observable<Optional<Patient>> {
    return database.patientDao()
        .patient(uuid)
        .toObservable()
        .map { patients ->
          when {
            patients.isNotEmpty() -> Just(patients.first())
            else -> None
          }
        }
  }

  fun mergeWithLocalData(serverPayloads: List<PatientPayload>): Completable {
    return serverPayloads
        .toObservable()
        .filter { payload ->
          val localCopy = database.patientDao().getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .toList()
        .flatMapCompletable { payloads ->
          Completable.fromAction {
            val newOrUpdatedAddresses = payloads.map { it.address.toDatabaseModel() }
            database.addressDao().save(newOrUpdatedAddresses)

            val newOrUpdatedPatients = payloads.map { it.toDatabaseModel(newStatus = DONE) }

            savePatients(newOrUpdatedPatients).blockingAwait()

            val newOrUpdatedPhoneNumbers = payloads
                .filter { it.phoneNumbers != null }
                .flatMap { patientPayload ->
                  patientPayload.phoneNumbers!!.map { it.toDatabaseModel(patientPayload.uuid) }
                }
            if (newOrUpdatedPhoneNumbers.isNotEmpty()) {
              database.phoneNumberDao().save(newOrUpdatedPhoneNumbers)
            }
          }
        }
  }

  fun ongoingEntry(): Single<OngoingPatientEntry> {
    return Single.fromCallable { ongoingPatientEntry }
  }

  fun saveOngoingEntry(ongoingEntry: OngoingPatientEntry): Completable {
    return Completable.fromAction {
      this.ongoingPatientEntry = ongoingEntry
    }
  }

  fun saveOngoingEntryAsPatient(): Single<Patient> {
    val cachedOngoingEntry = ongoingEntry().cache()

    val validation = cachedOngoingEntry
        .flatMapCompletable {
          val validationErrors = it.validationErrors(dobValidator)
          if (validationErrors.isEmpty()) {
            Completable.complete()
          } else {
            val errorNames = validationErrors.map { it.name }.toTypedArray()
            Completable.error(AssertionError("Patient entry has errors: ${Arrays.toString(errorNames)}"))
          }
        }

    val addressUuid = UUID.randomUUID()
    val addressSave = cachedOngoingEntry
        .map {
          with(it) {
            PatientAddress(
                uuid = addressUuid,
                colonyOrVillage = address!!.colonyOrVillage,
                district = address.district,
                state = address.state,
                createdAt = Instant.now(),
                updatedAt = Instant.now())
          }
        }
        .flatMapCompletable { address -> saveAddress(address) }

    val patientUuid = UUID.randomUUID()
    val sharedPatient = cachedOngoingEntry
        .map {
          with(it) {
            Patient(
                uuid = patientUuid,
                fullName = personalDetails!!.fullName,
                searchableName = nameToSearchableForm(personalDetails.fullName),
                gender = personalDetails.gender!!,
                status = PatientStatus.ACTIVE,

                dateOfBirth = convertToDate(personalDetails.dateOfBirth),
                age = personalDetails.age?.let {
                  Age(value = personalDetails.age.toInt(),
                      updatedAt = Instant.now(),
                      computedDateOfBirth = LocalDate.now().minusYears(personalDetails.age.toLong()))
                },

                addressUuid = addressUuid,

                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                syncStatus = PENDING)
          }
        }
        .cache()

    val patientSave = sharedPatient
        .flatMapCompletable { patient -> savePatient(patient) }

    val phoneNumberSave = cachedOngoingEntry
        .flatMapCompletable { entry ->
          if (entry.phoneNumber == null) {
            Completable.complete()
          } else {
            val number = with(entry.phoneNumber) {
              PatientPhoneNumber(
                  uuid = UUID.randomUUID(),
                  patientUuid = patientUuid,
                  phoneType = type,
                  number = number,
                  active = active,
                  createdAt = Instant.now(),
                  updatedAt = Instant.now())
            }
            savePhoneNumber(number)
          }
        }

    return validation
        .andThen(addressSave)
        .andThen(patientSave)
        .andThen(phoneNumberSave)
        .andThen(sharedPatient)
  }

  private fun convertToDate(dateOfBirth: String?): LocalDate? {
    return dateOfBirth?.let {
      val formatter = dateOfTimeFormatter
      formatter.parse(dateOfBirth, LocalDate::from)
    }
  }

  private fun saveAddress(address: PatientAddress): Completable {
    return Completable.fromAction {
      database.addressDao().save(address)
    }
  }

  fun address(addressUuid: UUID): Observable<Optional<PatientAddress>> {
    return database.addressDao()
        .address(addressUuid)
        .toObservable()
        .map { addresses ->
          when {
            addresses.isNotEmpty() -> Just(addresses.first())
            else -> None
          }
        }
  }

  private fun savePhoneNumber(number: PatientPhoneNumber): Completable {
    return Completable.fromAction {
      database.phoneNumberDao().save(listOf(number))
    }
  }

  fun phoneNumbers(patientUuid: UUID): Observable<Optional<PatientPhoneNumber>> {
    return database.phoneNumberDao()
        .phoneNumber(patientUuid)
        .toObservable()
        .map { numbers ->
          when {
            numbers.isNotEmpty() -> Just(numbers.first())
            else -> None
          }
        }
  }
}
