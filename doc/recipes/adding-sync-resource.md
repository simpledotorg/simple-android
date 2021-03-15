## Adding a new sync resource 

### Pre-requisites

 - A `Room` table where the data can be synced to and from like `BloodSugarMeasurement`

### Steps

Sync APIs are the ones where we pull from the server and push new records to the server.
Although some API might just pull from the server as well. Here we will only talk about the former.
Once the API contracts are finalized, we need to start with following to introduce the API in the code. We will use blood sugar APIs as an example here. 

 1. Start by creating a `@JsonClass` `data class` called `BloodSugarMeasurementPayload` and add the API response payload as per the contract.
 2. Add response class `BloodSugarPullResponse` as follows: 
 ```Kotlin
@JsonClass(generateAdapter = true)
data class BloodSugarPullResponse(

    @Json(name = "blood_sugars")
    override val payloads: List<BloodSugarMeasurementPayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<BloodSugarMeasurementPayload>

 ```

 3. Save the `processToken` in a shared preference and provide it through the Dagger DI graph.
 4. Create a Retrofit Api interface for adding the HTTP `GET` and `POST` requests for pulling and pushing records respectively.
 5. Add a new `Repository` that implements `SynceableRepository<T,P>`. Use the table `BloodSugarMeasurement` as `T` and payload class `BloodSugarMeasurementPayload` as `P`. This repository is used as an interface between the API and the database.
 6. Implement the overridden methods in the repository class by adding queries in the corresponding `Dao`. While most if the methods here are backed by straightforward queries, there is one in particular that can be challenging.
	 
   ```Kotlin
  override fun mergeWithLocalData(payloads: List<BloodSugarMeasurementPayload>): Completable {
    return payloads
        .toObservable()
        .filter { payload ->
          val localCopy = dao.getOne(payload.uuid)
          localCopy?.syncStatus.canBeOverriddenByServerCopy()
        }
        .map { it.toDatabaseModel(DONE) }
        .toList()
        .flatMapCompletable { Completable.fromAction { dao.save(it) } }
  }

   ```

 7. Introduce a new class that implements `ModelSync` which will be responsible for syncing. Follow any existing class like `BloodPressureSync` to implement the overridden methods. 
