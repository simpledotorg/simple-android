# ADR 013: SQL Performance Profiling (v2)

## Status

Accepted on 2021-06-14. Supercedes [012](./012-sql-performance-profiling.md).

## Context

In [012](./012-sql-performance-profiling.md), we described how we were planning to approach the problem of profiling SQL queries automatically. While
the approach worked in theory, practically we ran into issues with the build tooling for releasing the application. Android applications use a tool
called [R8](https://developer.android.com/studio/build/shrink-code) for optimizing release builds. As part of this process, the tool removes line
numbers from the `.class` files compiled by the Android toolchain. These line numbers are typically retained by most applications because they are
helpful in examining stack traces for crashes from the field. The tool provides an official way to retain these line numbers, and this is
described [here](https://developer.android.com/studio/build/shrink-code#decode-stack-trace).

In practice, we [discovered](https://github.com/vinaysshenoy/R8Test) that the tool would remove line numbers for release builds, regardless of whether
the official way to retain them was used or not. This posed a problem for the SQL performance profiling since we depended on the presence of line
numbers in order to detect which SQL query is being run.

In order to continue with the current method of SQL performance profiling, we would either need to:

- Wait until the issue with the R8 tool is fixed, or
- Turn off build optimization for release builds

Neither of these options were feasible at the time of writing this document, so we decided to rethink our approach to profiling SQL queries.

## Approach

Instead of a reflection based approach, we opted to go for a code modification approach. This required two steps to be done as part of the build
process:

#### Step 1: Insert profiling method

For every generated DAO, insert a method that delegates SQL performance information reporting to a class available at runtime. This method would look
something like:

```java
private static <T> T measureAndReport(final String methodName, final kotlin.jvm.functions.Function0<T> block) {
    final long start = System.currentTimeMillis();
    final T result;
    result = block.invoke();
    final java.time.Duration timeTaken = java.time.Duration.ofMillis(System.currentTimeMillis() - start);
    org.simple.clinic.SqlPerformanceReporter.report("UserRoomDao_Impl", methodName, timeTaken);
    return result;
 }
```

This would allow us to define a class, `org.simple.clinic.SqlPerformanceReporter`, whose `report` method would be called everytime
the `measureAndReport` method would be invoked.

#### Step 2: Delegate DAO query operation

The next step would then be to delegate the actual SQL query operations in every method in the DAO to the `measureAndReport` method. Considering a
generated method like

```java
@Override
public UUID currentFacilityUuid() {
    final String _sql = "SELECT currentFacilityUuid FROM LoggedInUser LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final UUID _result;
      if(_cursor.moveToFirst()) {
        final String _tmp;
        _tmp = _cursor.getString(0);
        _result = __uuidRoomTypeConverter.toUuid(_tmp);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
}
```

We then transformed this method so that it looked like

```java
@Override
public UUID currentFacilityUuid() {
    return measureAndReport("currentFacilityUuid", () -> {
      final String _sql = "SELECT currentFacilityUuid FROM LoggedInUser LIMIT 1";
      final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
      __db.assertNotSuspendingTransaction();
      final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
      try {
        final UUID _result;
        if (_cursor.moveToFirst()) {
          final String _tmp;
          _tmp = _cursor.getString(0);
          _result = __uuidRoomTypeConverter.toUuid(_tmp);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _cursor.close();
        _statement.release();
      }
    });
}
```

Adding this step to the build allowed us to automatically capture SQL performance profiling at whatever level of granularity we need.

### Implementation

The implementation of the build tool that transforms the generated Room DAOs is
available [here](https://github.com/simpledotorg/room-metadata-generator). The integration with the `simple-android` build process and the reporting
of the events to our analytics platform is available [here](https://github.com/simpledotorg/simple-android/pull/2703).

## Consequences

### Coupling with generated code

Since this approach uses code transformation, the generated code transformation is quite specfic to the code that Room generates. In the event that
Room decides to change the shape of the generated code, we might have to spend some effort on updating the tooling in order to continue reporting the
profiling events. The effort involved depends entirely on the degree to how much the shape of the generated code has changed, so it's hard to measure
the actual impact.

In practice, however, we don't expect the shape of the generated code to change drastically for a specfic major version of Room. Switching to major
version upgrades of Room will require investigation on how compatible it is with our build tooling before we proceed.

### No support for generated Kotlin code

We currently use [JavaParser](https://javaparser.org) to transform the original Room DAO and generate transformed source code. This framework,
however, only supports the Java language and not the Kotlin language. In the event that Room decides to generate Kotlin code instead of Java code, we
will need to look at an alternative approach to transforming the generated DAOs. A potential option might be to use the Kotlin compiler plugins API
with a framework like [Arrow Meta](https://meta.arrow-kt.io/).
