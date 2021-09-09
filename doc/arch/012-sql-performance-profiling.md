# ADR 012: SQL Performance Profiling

## Status

Superceded by [013](./013-sql-performance-profiling-v2.md) on 2021-06-14.

## Context

The Simple Android app is driven almost entirely by the local database. While this is great for the application's reliability in inconsistent or poor
network conditions, this can also be a problem in the event of running expensive database queries when the amount of data being queried is large.

This has historically led to cases where we tend to look at optimizing performance of specific screens _AFTER_ it becomes a problem in `PRODUCTION`
and we receive reports from the field. This leads to a degraded user experience for a significant portion of the user base for however much time as it
takes to fix issues after they are reported, plus additional stress to the team working on fixing the performance issues while the problems are
ongoing in the field.

## Goal

The overall goal is to reach a state where we collect performance information for SQL queries automatically in the field and report it to an
appropriate tool, so we can expect performance issues before they reach a problematic stage in `PRODUCTION` and fix them proactively.

## Approach

We currently use the [Room database](https://developer.android.com/training/data-storage/room) framework for defining our database entities and
models. This gives us a bunch of benefits, but also introduces a layer of abstraction that does not allow us to do automatic query profiling easily:

- The library works by having the developers define interfaces with annotations that describe the SQL queries to be run.
- The Gradle plugins then process the interfaces defined to generate actual implementations which are not directly available for developers to add
  profiling code in.

#### Sample interface definition

```kotlin
  @Dao
  abstract class RoomDao {

    @Query("SELECT * FROM LoggedInUser LIMIT 1")
    abstract fun user(): Flowable<List<User>>

    @Query("SELECT * FROM LoggedInUser LIMIT 1")
    abstract fun userImmediate(): User?
  }
```

#### Sample generated implementation

```java
public final class UserRoomDao_Impl extends User.RoomDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<User> __insertionAdapterOfUser;

  private final UuidRoomTypeConverter __uuidRoomTypeConverter = new UuidRoomTypeConverter();

  private final UserStatus.RoomTypeConverter __roomTypeConverter = new UserStatus.RoomTypeConverter();

  private final InstantRoomTypeConverter __instantRoomTypeConverter = new InstantRoomTypeConverter();

  private final User.LoggedInStatus.RoomTypeConverter __roomTypeConverter_1 = new User.LoggedInStatus.RoomTypeConverter();

  private final User.CapabilityStatus.RoomTypeConverter __roomTypeConverter_2 = new User.CapabilityStatus.RoomTypeConverter();

  private final EntityDeletionOrUpdateAdapter<User> __deletionAdapterOfUser;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLoggedInStatusForUser;

  private final SharedSQLiteStatement __preparedStmtOfSetCurrentFacility;

  private final SyncStatus.RoomTypeConverter __roomTypeConverter_3 = new SyncStatus.RoomTypeConverter();

  public UserRoomDao_Impl(RoomDatabase __db) {
    this.__db = __db;
  }


  @Override
  public Flowable<List<User>> user() {
    final String _sql = "SELECT * FROM LoggedInUser LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return RxRoom.createFlowable(__db, false, new String[]{"LoggedInUser"}, new Callable<List<User>>() {
      @Override
      public List<User> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfPinDigest = CursorUtil.getColumnIndexOrThrow(_cursor, "pinDigest");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfLoggedInStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "loggedInStatus");
          final int _cursorIndexOfRegistrationFacilityUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "registrationFacilityUuid");
          final int _cursorIndexOfCurrentFacilityUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "currentFacilityUuid");
          final int _cursorIndexOfTeleconsultPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "teleconsultPhoneNumber");
          final int _cursorIndexOfCanTeleconsult = CursorUtil.getColumnIndexOrThrow(_cursor, "capability_canTeleconsult");
          final List<User> _result = new ArrayList<User>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final User _item;
            final UUID _tmpUuid;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfUuid);
            _tmpUuid = __uuidRoomTypeConverter.toUuid(_tmp);
            final String _tmpFullName;
            _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            final String _tmpPhoneNumber;
            _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            final String _tmpPinDigest;
            _tmpPinDigest = _cursor.getString(_cursorIndexOfPinDigest);
            final UserStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __roomTypeConverter.toEnum(_tmp_1);
            final Instant _tmpCreatedAt;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfCreatedAt);
            _tmpCreatedAt = __instantRoomTypeConverter.toInstant(_tmp_2);
            final Instant _tmpUpdatedAt;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfUpdatedAt);
            _tmpUpdatedAt = __instantRoomTypeConverter.toInstant(_tmp_3);
            final User.LoggedInStatus _tmpLoggedInStatus;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfLoggedInStatus);
            _tmpLoggedInStatus = __roomTypeConverter_1.toEnum(_tmp_4);
            final UUID _tmpRegistrationFacilityUuid;
            final String _tmp_5;
            _tmp_5 = _cursor.getString(_cursorIndexOfRegistrationFacilityUuid);
            _tmpRegistrationFacilityUuid = __uuidRoomTypeConverter.toUuid(_tmp_5);
            final UUID _tmpCurrentFacilityUuid;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfCurrentFacilityUuid);
            _tmpCurrentFacilityUuid = __uuidRoomTypeConverter.toUuid(_tmp_6);
            final String _tmpTeleconsultPhoneNumber;
            _tmpTeleconsultPhoneNumber = _cursor.getString(_cursorIndexOfTeleconsultPhoneNumber);
            final User.Capabilities _tmpCapabilities;
            if (! (_cursor.isNull(_cursorIndexOfCanTeleconsult))) {
              final User.CapabilityStatus _tmpCanTeleconsult;
              final String _tmp_7;
              _tmp_7 = _cursor.getString(_cursorIndexOfCanTeleconsult);
              _tmpCanTeleconsult = __roomTypeConverter_2.toEnum(_tmp_7);
              _tmpCapabilities = new User.Capabilities(_tmpCanTeleconsult);
            }  else  {
              _tmpCapabilities = null;
            }
            _item = new User(_tmpUuid,_tmpFullName,_tmpPhoneNumber,_tmpPinDigest,_tmpStatus,_tmpCreatedAt,_tmpUpdatedAt,_tmpLoggedInStatus,_tmpRegistrationFacilityUuid,_tmpCurrentFacilityUuid,_tmpTeleconsultPhoneNumber,_tmpCapabilities);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public User userImmediate() {
    final String _sql = "SELECT * FROM LoggedInUser LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfPinDigest = CursorUtil.getColumnIndexOrThrow(_cursor, "pinDigest");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfLoggedInStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "loggedInStatus");
      final int _cursorIndexOfRegistrationFacilityUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "registrationFacilityUuid");
      final int _cursorIndexOfCurrentFacilityUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "currentFacilityUuid");
      final int _cursorIndexOfTeleconsultPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "teleconsultPhoneNumber");
      final int _cursorIndexOfCanTeleconsult = CursorUtil.getColumnIndexOrThrow(_cursor, "capability_canTeleconsult");
      final User _result;
      if(_cursor.moveToFirst()) {
        final UUID _tmpUuid;
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfUuid);
        _tmpUuid = __uuidRoomTypeConverter.toUuid(_tmp);
        final String _tmpFullName;
        _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        final String _tmpPhoneNumber;
        _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        final String _tmpPinDigest;
        _tmpPinDigest = _cursor.getString(_cursorIndexOfPinDigest);
        final UserStatus _tmpStatus;
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
        _tmpStatus = __roomTypeConverter.toEnum(_tmp_1);
        final Instant _tmpCreatedAt;
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfCreatedAt);
        _tmpCreatedAt = __instantRoomTypeConverter.toInstant(_tmp_2);
        final Instant _tmpUpdatedAt;
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfUpdatedAt);
        _tmpUpdatedAt = __instantRoomTypeConverter.toInstant(_tmp_3);
        final User.LoggedInStatus _tmpLoggedInStatus;
        final String _tmp_4;
        _tmp_4 = _cursor.getString(_cursorIndexOfLoggedInStatus);
        _tmpLoggedInStatus = __roomTypeConverter_1.toEnum(_tmp_4);
        final UUID _tmpRegistrationFacilityUuid;
        final String _tmp_5;
        _tmp_5 = _cursor.getString(_cursorIndexOfRegistrationFacilityUuid);
        _tmpRegistrationFacilityUuid = __uuidRoomTypeConverter.toUuid(_tmp_5);
        final UUID _tmpCurrentFacilityUuid;
        final String _tmp_6;
        _tmp_6 = _cursor.getString(_cursorIndexOfCurrentFacilityUuid);
        _tmpCurrentFacilityUuid = __uuidRoomTypeConverter.toUuid(_tmp_6);
        final String _tmpTeleconsultPhoneNumber;
        _tmpTeleconsultPhoneNumber = _cursor.getString(_cursorIndexOfTeleconsultPhoneNumber);
        final User.Capabilities _tmpCapabilities;
        if (! (_cursor.isNull(_cursorIndexOfCanTeleconsult))) {
          final User.CapabilityStatus _tmpCanTeleconsult;
          final String _tmp_7;
          _tmp_7 = _cursor.getString(_cursorIndexOfCanTeleconsult);
          _tmpCanTeleconsult = __roomTypeConverter_2.toEnum(_tmp_7);
          _tmpCapabilities = new User.Capabilities(_tmpCanTeleconsult);
        }  else  {
          _tmpCapabilities = null;
        }
        _result = new User(_tmpUuid,_tmpFullName,_tmpPhoneNumber,_tmpPinDigest,_tmpStatus,_tmpCreatedAt,_tmpUpdatedAt,_tmpLoggedInStatus,_tmpRegistrationFacilityUuid,_tmpCurrentFacilityUuid,_tmpTeleconsultPhoneNumber,_tmpCapabilities);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
```

As shown above, there are two broad categories of queries generally used throughout the app.

#### Synchronous queries

`userImmediate()` in the `RoomDao` described earlier is an example of a synchronous query. These queries execute on the same thread that they are
invoked in and directly query the database. These are relatively easier to instrument since we can just measure the time taken to execute the method
and report it to tracking software. An approach to doing this automatically would have been to use something
like [dynamic proxies](https://www.baeldung.com/java-dynamic-proxies) which allows us to replace the implementation of an interface at runtime,

However, the second type of query that we use in a lot of places in the app will not work with this method.

#### Reactive, asynchronous queries

`user()` in the `RoomDao` described earlier is an example of a reactive asynchronous query. As can be seen, the return type of these methods are one
of the [io.reactivex](https://github.com/ReactiveX/RxJava/tree/2.x#base-classes) primitive types. These have an advantage that when they are used to
power the UI of the application, any background changes to the data that they load will automatically update the UI without need for the application
to explicitly requery data.

This makes building UI quite easy. However, this makes measuring performance hard. Measuring how much time it takes to invoke the database method will
not give us the amount of time required to query the database, it will only give us the time required to instantiate the reactive type instance. The
time that we are looking to measure is the time taken when the reactive instance returned is subscribed to.

This is hard to measure automatically in the Room database as we have no way to access the reactive type that is instantiated within the generated DAO
implementation. It is possible to manually measure this, but it is error prone since it requires every developer to manually instrument queries that
they write and is easy to miss.

### Approach 1: Bytecode processing

Since the Room library generates database implementations which we cannot access, the first approach attempted was to process and edit the bytecode
that was a part of the final APK. We tried using this library called [Javassist](https://www.javassist.org/) to insert bytecode statements into
the `.class` files of the generated DAO implementations to track time and measure the database query performance.

While this approach was doable, it turned out to be quite complex. The generated bytecode was deeply coupled to the version of the Room library. It
would also require a significant learning curve by developers needing to understand JVM bytecode in order to work with this approach and was deemed to
be too expensive in order to use as the final approach.

### Approach 2: Java Abstract Syntax Tree (AST) processing

This approach required a mixture of runtime code and processing of the generated code. The way this works is by two separate components working
together:

1. We integrate a tool into the Simple Android build process that processes the Java code (using [Javaparser](https://javaparser.org/)) generated by
   Room and outputs some metadata which gets embedded as part of the final APK as a raw asset file. This metadata contains some information about the
   generated code, like:

- What is the name of the generated DAO file?
- What are all the methods in that DAO file?
- What are the beginning and ending line numbers of the method?

```csv
UserRoomDao_Impl,user,262,346
UserRoomDao_Impl,userImmediate,348,424
```

A proof of concept for this tool is available at https://github.com/simpledotorg/room-metadata-generator.

2. A runtime wrapper around the [`SupportSQLiteOpenHelper`](https://developer.android.com/reference/androidx/sqlite/db/SupportSQLiteOpenHelper) class.
   This wrapper processes the previously generated metadata packaged into the app and is provided to the `Room` database as the SQLite implementation
   to use. Then, whenever a database query is made, we manually create a [`Throwable`](https://developer.android.com/reference/java/lang/Throwable)
   instance which has a full stacktrace of the method calls, including the line number of the method call.

We then walk the stacktrace until we find out which generated DAO line this SQLite method was invoked from, and then perform a reverse lookup on the
generated DAO metadata to find out exactly which method was responsible was invoking this SQLite method. From here, it is fairly trivial to track the
amount of time required and report this to the reporting tool of choice.

A sample of queries run and the time taken shows it is quite effective:

```shell
2021-03-11 16:26:41.366 I/SearchPerf: Time taken for UserRoomDao_Impl.userImmediate: 4426 ms
2021-03-11 16:26:42.100 I/SearchPerf: Time taken for UserRoomDao_Impl.currentFacility: 0 ms
2021-03-11 16:26:42.875 I/SearchPerf: Time taken for UserRoomDao_Impl.user: 1 ms
2021-03-11 16:26:42.877 I/SearchPerf: Time taken for UserRoomDao_Impl.userAndFacilityDetails: 0 ms
2021-03-11 16:26:42.942 I/SearchPerf: Time taken for PatientRoomDao_Impl.patientCount: 15 ms
2021-03-11 16:26:42.942 I/SearchPerf: Time taken for UserRoomDao_Impl.currentFacility: 15 ms
2021-03-11 16:26:42.944 I/SearchPerf: Time taken for MedicalHistoryRoomDao_Impl.count: 14 ms
2021-03-11 16:26:42.945 I/SearchPerf: Time taken for AppointmentRoomDao_Impl.count: 15 ms
2021-03-11 16:26:43.290 I/SearchPerf: Time taken for PrescribedDrugRoomDao_Impl.count: 304 ms
2021-03-11 16:26:43.290 I/SearchPerf: Time taken for UserRoomDao_Impl.userImmediate: 183 ms
2021-03-11 16:26:43.292 I/SearchPerf: Time taken for UserRoomDao_Impl.currentFacilityImmediate: 165 ms
2021-03-11 16:26:43.293 I/SearchPerf: Time taken for TeleconsultRecordRoomDao_Impl.count: 151 ms
2021-03-11 16:26:43.294 I/SearchPerf: Time taken for BloodSugarMeasurementRoomDao_Impl.count: 145 ms
2021-03-11 16:26:43.295 I/SearchPerf: Time taken for BloodPressureMeasurementRoomDao_Impl.count: 65 ms
2021-03-11 16:26:43.300 I/SearchPerf: Time taken for RecentPatientRoomDao_Impl.recentPatients: 9 ms
```

This approach seems promising, so we decided to proceed with it.

## Consequences

### Performance Impact

One more thing we wanted to make sure of is that the work required to implement this instrumentation itself does not impact the application
performance much.

- The most expensive part of this performance tracking implementation is creating a `Throwable` and filling in the stacktrace. This takes at most 1
  millisecond on a low end device ([Samsung Galaxy M01 Core](https://www.gsmarena.com/samsung_galaxy_m01_core-10316.php)), so the impact is atleast an
  order of magnitude less than the time taken to run the queries themselves.
- We do not need to profile _every single_ query that runs in the app. What is more important for us is to get a sense of which queries are degrading
  faster with scale. For this, we can randomly sample a small percentage of queries run, which should give us enough data over time across the entire
  userbase.

### Limitations

#### Minification tooling

- We will rely on build tooling to process generated code to extract method metadata. This works for now because we use the Android minification
  toolchain _only_ for minification and not for obfuscation. If we decide to turn on code obfuscation for some reason, we would also need to update
  the build tooling to either:
  - Skip obfuscation for generated Room DAO files, or
  - Process the mapping files generated by the obfuscation tooling to update the generated metadata to retain this information

This is unlikely to be a problem since the application is open source, so there is no need to obfuscate the final APK in any way.

- We currentlly have the minification tooling to retain line numbers in the `.class` files so that lne numbers are present in stack traces. Changing
  this would remove line numbers from the stacktrace that gets generated at runtime and preventing the tool from working.

A simple way to guard against this would be to add comments in the minification configuration file so that anyone who changes the file will be warned
that they will also affect these other parts of the app.

#### Paging library

We currently use the [Paging library](https://developer.android.com/topic/libraries/architecture/paging/) in order to lazy load some of the lists in
the app. Due to an implementation detail of how Room generates these classes, the performance tooling is not able to determine which DAO method is
called when a paginated source invokes a database query. These queries will be ignored from reporting.

We currentlly use paginated lists in three features in the app, only one of which is used frequently (the `Overdue` feature). What would be a good
solution for now is to be explicitly aware of the fact that these queries will not be tracked to begin with, and start collecting data from the rest
of the queries.

We can then investigate adding support for these queries as soon as we have the rest of the reporting and instrumentation in place.

#### Overloaded methods

The current tooling implementation only reports the method names as part of the metadata. However, there are quite a few instances where we have DAO
methods with the same name, but different signatures (parameters + method name). This will confuse reporting since all performance information for
overloaded methods will be reported under the same name, when they should be different.

We could approach this in one of three ways:

- Fail the build if overloaded DAO methods were found and report them as a build error so that they can be renamed.
- Make the entire method signature a part of the metadata by combining the parameters and the method name in order to generate a final metric name.
  This will result in metric names that are hard to read since someone looking up the metrics will need to look up the method with a specific
  signature.
- Use source annotations in order to define the metric name that will be reported instead of the actual method name. We can update the tooling to look
  for specific source level annotations on overloaded DAO methods which we can use as the name of the metric instead of the actual method name. The
  tooling can even enforce this by failing the build if there are overloaded methods without the source annotations being present.

We chose to go with the first approach for failing the build since it was the simplest approach.
