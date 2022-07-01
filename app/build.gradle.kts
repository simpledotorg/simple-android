import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.simple.rmg.RoomMetadataGenerator

repositories {
  maven(url = "https://jitpack.io")
}

plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("kapt")
  id("kotlin-parcelize")
  id("io.sentry.android.gradle")
  id("plugins.git.install-hooks")
  id("com.datadoghq.dd-sdk-android-gradle-plugin")
  id("androidx.benchmark")
}

sentry {
  includeProguardMapping.set(true)
  autoUploadProguardMapping.set(false)

  // We are using our own instrumentation tooling for Room queries
  // Look at [ADR 013: SQL Performance Profiling (v2)]
  tracingInstrumentation {
    enabled.set(false)
  }
}

kapt {
  arguments {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", true)
    arg("room.expandProjection", true)
  }
}

tasks.withType<Test> {
  testLogging {
    // set options for log level LIFECYCLE
    events = setOf(
        TestLogEvent.PASSED,
        TestLogEvent.SKIPPED,
        TestLogEvent.FAILED,
        TestLogEvent.STANDARD_OUT
    )
    showExceptions = true
    exceptionFormat = TestExceptionFormat.FULL
    showCauses = true
    showStackTraces = true

    // set options for log level DEBUG and INFO
    debug {
      events = setOf(
          TestLogEvent.STARTED,
          TestLogEvent.PASSED,
          TestLogEvent.SKIPPED,
          TestLogEvent.FAILED,
          TestLogEvent.STANDARD_OUT,
          TestLogEvent.STANDARD_ERROR
      )
    }
    info.events = debug.events

    addTestListener(object : TestListener {
      override fun beforeSuite(descriptor: TestDescriptor?) {}
      override fun afterSuite(descriptor: TestDescriptor?, result: TestResult?) {
        if (descriptor?.parent != null && result != null) { // will match the outermost suite
          val output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)"
          val startItem = "|  "
          val endItem = "  |"
          val repeatLength = startItem.length + output.length + endItem.length
          println("\n" + ("-".repeat(repeatLength)) + "\n" + startItem + output + endItem + "\n" + ("-".repeat(repeatLength)))
        }
      }

      override fun beforeTest(descriptor: TestDescriptor?) {}
      override fun afterTest(descriptor: TestDescriptor?, result: TestResult?) {}
    })
  }
}

android {
  namespace = "org.simple.clinic"

  val androidNdkVersion: String by project
  val compileSdkVersion: Int by rootProject.extra
  val minSdkVersion: Int by rootProject.extra
  val targetSdkVersion: Int by rootProject.extra

  compileSdk = compileSdkVersion
  // Needed to switch NDK versions on the CI server since they have different
  // NDK versions on macOS and Linux environments. Gradle plugin 3.6+ requires
  // us to pin an NDK version if we package native libs.
  // https://developer.android.com/studio/releases/gradle-plugin#default-ndk-version
  //
  // Currently, this is only used for assembling the APK where the build process
  // strips debug symbols from the APK.
  ndkVersion = androidNdkVersion

  defaultConfig {
    applicationId = "org.simple.clinic"
    minSdk = minSdkVersion
    targetSdk = targetSdkVersion
    versionCode = 1
    versionName = "0.1"
    multiDexEnabled = true

    testInstrumentationRunner = "org.simple.clinic.AndroidTestJUnitRunner"

    val defaultProguardFile: String by project
    proguardFiles(
        getDefaultProguardFile(defaultProguardFile),
        "proguard-rules.pro"
    )

    vectorDrawables.useSupportLibrary = true

    val sentryDsn: String by project
    val sentryEnvironment: String by project
    val mixpanelToken: String by project
    val manifestEndpoint: String by project
    val disableScreenshot: String by project
    val allowRootedDevice: String by project
    val datadogServiceName: String by project
    val datadogApplicationId: String by project
    val datadogClientToken: String by project
    val datadogEnvironment: String by project

    addManifestPlaceholders(mapOf(
        "sentryDsn" to sentryDsn,
        "sentryEnvironment" to sentryEnvironment
    ))

    buildConfigField("String", "MIXPANEL_TOKEN", "\"$mixpanelToken\"")
    buildConfigField("String", "MANIFEST_ENDPOINT", "\"$manifestEndpoint\"")
    buildConfigField("boolean", "DISABLE_SCREENSHOT", disableScreenshot)
    buildConfigField("boolean", "ALLOW_ROOTED_DEVICE", allowRootedDevice)
    buildConfigField("String", "DATADOG_SERVICE_NAME", "\"$datadogServiceName\"")
    buildConfigField("String", "DATADOG_APPLICATION_ID", "\"$datadogApplicationId\"")
    buildConfigField("String", "DATADOG_CLIENT_TOKEN", "\"$datadogClientToken\"")
    buildConfigField("String", "DATADOG_ENVIRONMENT", "\"$datadogEnvironment\"")
  }

  buildTypes {
    getByName("debug") {
      applicationIdSuffix = ".debug"
      isMinifyEnabled = false
      isShrinkResources = false
    }

    val runProguard: String by project
    getByName("release") {
      isDebuggable = false
      isMinifyEnabled = runProguard.toBoolean()
      isShrinkResources = runProguard.toBoolean()
    }
  }

  buildFeatures {
    viewBinding = true
  }

  flavorDimensions.add("track")

  productFlavors {
    create("qa") {
      dimension = "track"
      applicationIdSuffix = ".qa"
      versionNameSuffix = "-qa"
    }

    create("staging") {
      dimension = "track"
      applicationIdSuffix = ".staging"
      versionNameSuffix = "-demo"
    }

    create("sandbox") {
      dimension = "track"
      applicationIdSuffix = ".sandbox"
      versionNameSuffix = "-sandbox"
    }

    create("security") {
      dimension = "track"
      applicationIdSuffix = ".security"
      versionNameSuffix = "-security"
    }

    create("production") {
      dimension = "track"
    }
  }

  androidComponents {
    val filteredVariants = setOf(
        "qaRelease",
        "stagingDebug",
        "sandboxDebug",
        "productionDebug",
        "securityDebug"
    )

    beforeVariants { variant ->
      variant.enabled = variant.name !in filteredVariants
    }
  }

  lint {
    warningsAsErrors = true
    abortOnError = true
    checkReleaseBuilds = false
    checkDependencies = true
    ignoreTestSources = true
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
  }

  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
  }

  sourceSets {
    getByName("androidTest") {
      assets.srcDirs(files("$projectDir/schemas"))
    }

    getByName("main") {
      assets.srcDirs("${project.buildDir}/generated/assets/room_dao_metadata/")
    }
  }

  packagingOptions {
    // Deprecated ABIs. See https://developer.android.com/ndk/guides/abis
    jniLibs.excludes.add("lib/mips/libsqlite3x.so")
    jniLibs.excludes.add("lib/mips64/libsqlite3x.so")
    jniLibs.excludes.add("lib/armeabi/libsqlite3x.so")
  }

  bundle {
    language {
      // Specifies that the app bundle should not support configuration APKs for language resources.
      // These resources are instead packaged with each base and dynamic feature APK.
      // We are doing this since we support switching the language within the app and we want all
      // translations to be delivered to all devices.
      enableSplit = false
    }
  }

  // We don"t obfuscate (only minify) using proguard. Gradle plugin 3.2.0 (and greater?) generates
  // an empty mappings.txt file. This caused an issue where the CI deploy to play store task tries
  // to upload the empty mapping file, which causes the Play Store api to complain.
  val deleteProguardMappings = tasks.create<Delete>("deleteProguardMappings") {
    delete(fileTree(buildDir).matching {
      include("outputs/mapping/**/mapping.txt")
    })
  }

  afterEvaluate {
    val assembleReleaseTasks = setOf(
        "assembleStagingRelease",
        "assembleSandboxRelease",
        "assembleProductionRelease",
        "assembleSecurityRelease"
    )

    assembleReleaseTasks.forEach { buildType ->
      tasks.findByName(buildType)?.finalizedBy(deleteProguardMappings)
    }

    val kaptTasks = mapOf(
        "kaptQaDebugKotlin" to "qaDebug",
        "kaptSandboxReleaseKotlin" to "sandboxRelease",
        "kaptStagingReleaseKotlin" to "stagingRelease",
        "kaptSecurityReleaseKotlin" to "securityRelease",
        "kaptProductionReleaseKotlin" to "productionRelease"
    )

    kaptTasks.forEach { (buildType, sourceSetName) ->
      val taskQualifier = buildType
          .replace("kapt", "")
          .replace("Kotlin", "")

      val taskName = "transform${taskQualifier}GeneratedRoomDao"
      val transformRoomDaoTask = tasks.create<TransformGeneratedRoomDaoTask>(taskName) {
        sourceSet.set(sourceSetName)
        reporterClassName.set("org.simple.clinic.storage.monitoring.SqlPerformanceReporter")
      }

      tasks.findByName(buildType)?.finalizedBy(transformRoomDaoTask)
      tasks.named("compile${taskQualifier}JavaWithJavac").configure {
        dependsOn(transformRoomDaoTask)
      }
    }
  }
}

dependencies {
  /**
   * Debug dependencies
   */
  debugImplementation(libs.faker)
  debugImplementation(libs.bundles.flipper)
  debugImplementation(libs.leakcanary)
  debugImplementation(libs.soloader)

  /**
   * Prod dependencies
   */
  implementation(libs.androidx.annotation.annotation)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.cardview)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.preference)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.viewpager2)

  implementation(libs.bundles.androidx.camera)

  implementation(libs.bundles.androidx.paging)

  implementation(libs.bundles.androidx.room)
  kapt(libs.androidx.room.compiler)

  implementation(libs.bundles.androidx.work)

  implementation(libs.bundles.moshi)
  kapt(libs.moshi.codegen)

  implementation(libs.bundles.okhttp)

  implementation(libs.bundles.retrofit)

  implementation(libs.bundles.rx.binding)

  implementation(libs.dagger.dagger)
  kapt(libs.dagger.compiler)

  implementation(libs.edittext.masked)
  implementation(libs.edittext.pinentry)

  implementation(libs.firebase.config)

  implementation(libs.itemanimators)

  implementation(libs.itext7)

  implementation(libs.jbcrypt)

  implementation(libs.kotlin.coroutines)
  implementation(libs.kotlin.coroutines.test)
  implementation(libs.kotlin.stdlib)

  implementation(libs.logback.classic)

  implementation(libs.lottie)

  implementation(libs.material)

  implementation(libs.mixpanel.android)

  implementation(libs.okhttp.interceptor.logging)

  implementation(libs.openCsv) {
    exclude(module = "commons-logging")
  }

  implementation(libs.play.core)
  implementation(libs.play.services.auth)
  implementation(libs.play.services.location)
  implementation(libs.play.services.mlkit.barcode)

  implementation(libs.rootbeer)

  implementation(libs.rx.android)
  implementation(libs.rx.java)
  implementation(libs.rx.kotlin)
  implementation(libs.rx.preferences)

  implementation(libs.sentry.android) {
    exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
  }

  implementation(libs.signaturepad)

  implementation(libs.sqlite.android)

  implementation(libs.threeten.extra)

  implementation(libs.traceur)

  implementation(libs.uuid.generator)

  implementation(libs.viewpump)

  implementation(libs.zxing)

  implementation(projects.mobiusBase)
  implementation(projects.simplePlatform)
  implementation(projects.simpleVisuals)

  /**
   * Unit test dependencies
   */
  testImplementation(projects.mobiusMigration)

  testImplementation(libs.androidx.paging.common)

  testRuntimeOnly(libs.asm) {
    because("not mandatory, but Truth recommends adding this dependency for better error reporting")
  }

  testImplementation(libs.faker)

  testImplementation(libs.junit)
  testImplementation(libs.junitParams)

  testImplementation(libs.mobius.test)

  testImplementation(libs.mockito.kotlin)

  testImplementation(libs.truth)

  testImplementation(libs.kotlin.reflect)

  testImplementation(projects.sharedTestCode)

  /**
   * Android test dependencies
   */
  androidTestImplementation(libs.androidx.annotation.annotation)
  androidTestImplementation(libs.androidx.archCoreTesting) {
    // This dependency transitively pulls in a newer version of Mockito than Mockito-Kotlin does.
    // This results in the import statements in the unit tests breaking for mockito methods because
    // the IDE uses the version of Mockito that is present in the classpath from the androidTest
    // sourceset dependency, which raises an inspection error in the IDE. We can explicitly exclude
    // it from this dependency because it is also pulled in via mockito-kotlin.
    exclude(group = "org.mockito", module = "mockito-core")
  }

  androidTestImplementation(libs.bundles.androidx.test)

  androidTestRuntimeOnly(libs.asm) {
    because("not mandatory, but Truth recommends adding this dependency for better error reporting")
  }

  androidTestImplementation(libs.faker)

  androidTestImplementation(libs.androidx.room.testing)

  androidTestImplementation(libs.truth)

  kaptAndroidTest(libs.dagger.compiler)

  androidTestImplementation(projects.sharedTestCode)

  /**
   * Misc
   */
  coreLibraryDesugaring(libs.android.desugaring)

  lintChecks(projects.lint)

  runtimeOnly(libs.jackson.core)

  implementation(libs.datadog.sdk)

  androidTestImplementation(libs.apache.commons.math)
}

// This must always be present at the bottom of this file, as per:
// https://console.firebase.google.com/u/2/project/simple-org/settings/general/
apply(plugin = "com.google.gms.google-services")

abstract class TransformGeneratedRoomDaoTask : DefaultTask() {

  @get:Input
  abstract val sourceSet: Property<String>

  @get:Input
  abstract val reporterClassName: Property<String>

  @TaskAction
  fun run() {
    val rmg = RoomMetadataGenerator()

    rmg.run(project.projectDir.absolutePath, sourceSet.get(), reporterClassName.get())
  }
}
