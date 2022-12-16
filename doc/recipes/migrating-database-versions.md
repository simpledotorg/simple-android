## Migrating database versions

Database migrations are done using [Room](https://developer.android.com/training/data-storage/room/migrating-db-versions). These migrations are run 
when the app is opened for the first time after an update.

### Steps

1. Make required changes to the database schema.
2. Increment the database version by 1 in the `AppDatabase` class. Then build the project to generate the new schema file. Which is stored in the
   `app/schemas` directory. This will be used for testing the migration in later steps.
3. Create a new migration class `Migration_XX` which extend `Migration` class. It takes in start and end versions as parameters. The `XX` indicates
   the version you are migrating to. You can add the migration steps in the `migrate` method. Make sure to run the migration steps in
   `database.inTransaction` block to run them in a transaction. We are also annotating the constructor with `Inject` to provide it as part of Dagger
   graph, so that we can provide it in the database builder.
   ```Kotlin
    @Suppress("ClassName")
    class Migration_100 @Inject constructor() : Migration(99, 100) {
      override fun migrate(database: SupportSQLiteDatabase) {
        database.inTransaction {
          execSQL("""
            CREATE TABLE IF NOT EXISTS `NewTable` (
              `uuid` TEXT NOT NULL, 
              `name` TEXT NOT NULL, 
              PRIMARY KEY(`uuid`)
            )
          """)
        }
      }
    }
   ```
4. Add the migration class to the `RoomMigrationsModule` class.
   ```Kotlin
   @Provides
   fun databaseMigration(
    migration_100: Migration_100
   ): List<Migration> {
     return listOf(
         migration_100
     )
   }
   ```
5. Run the app and verify that the migration is successful.

### Testing

While you can manually verify the database migrations by running the app and verifying the data. It's recommended to write automated tests for
migrations.

1. Create an integration test in `storage/migrations` package in `androidTest` source. The test should extend `BaseDatabaseMigrationTest` class, it
   takes in a start and end version. The test should run the migration steps and verify that the migration is successful.
   ```Kotlin
   class Migration100AndroidTest : BaseDatabaseMigrationTest(99, 100) {
     @Test
     fun migration_should_create_new_table() {
        // Verify migration steps here
     }
   }
   ```
2. In the test function you can access `before` and `after` database instances. The `before` database instance is the database before the migration
   and the `after` database instance is the database after the migration. You can use these instances to verify that the migration is successful.
   ```Kotlin
    before.assertTableDoesNotExist("NewTable")
    after.assertTableExists("NewTable")
   ```
