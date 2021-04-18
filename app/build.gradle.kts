import com.android.build.api.variant.VariantFilter
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
  id("com.google.firebase.firebase-perf")
}

apply(from = "../quality/install-git-hook.gradle")

sentry {
  autoProguardConfig = false
  // We can disable auto upload of proguard mappings since we use -dontobfuscate when running proguard
  autoUpload = false
}

kapt {
  arguments {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", false)
    arg("room.expandProjection", false)
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
      override fun afterSuite(descriptor: TestDescriptor?, result: TestResult?) {}
      override fun beforeTest(descriptor: TestDescriptor?) {}
      override fun afterTest(descriptor: TestDescriptor?, result: TestResult?) {
        if (descriptor?.parent != null && result != null) { // will match the outermost suite
          val output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)"
          val startItem = "|  "
          val endItem = "  |"
          val repeatLength = startItem.length + output.length + endItem.length
          println("\n" + ("-".repeat(repeatLength)) + "\n" + startItem + output + endItem + "\n" + ("-".repeat(repeatLength)))
        }
      }
    })
  }
}

android {
  compileSdkVersion(versions.compileSdk)
  // Needed to switch NDK versions on the CI server since they have different
  // NDK versions on macOS and Linux environments. Gradle plugin 3.6+ requires
  // us to pin an NDK version if we package native libs.
  // https://developer.android.com/studio/releases/gradle-plugin#default-ndk-version
  //
  // Currently, this is only used for assembling the APK where the build process
  // strips debug symbols from the APK.
  val androidNdkVersion: String by project
  ndkVersion = androidNdkVersion

  defaultConfig {
    applicationId = "org.simple.clinic"
    minSdkVersion(versions.minSdk)
    targetSdkVersion(versions.compileSdk)
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

    addManifestPlaceholders(mapOf(
        "sentryDsn" to sentryDsn,
        "sentryEnvironment" to sentryEnvironment
    ))
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

  flavorDimensions("track")

  productFlavors {
    create("qa") {
      val qaManifestEndpoint: String by project
      val qaFallbackApiEndpoint: String by project
      val qaMixpanelToken: String by project

      dimension = "track"
      applicationIdSuffix = ".qa"
      versionNameSuffix = "-qa"
      buildConfigField("String", "MANIFEST_ENDPOINT", "\"$qaManifestEndpoint\"")
      buildConfigField("String", "FALLBACK_ENDPOINT", "\"$qaFallbackApiEndpoint\"")
      buildConfigField("String", "MIXPANEL_TOKEN", "\"$qaMixpanelToken\"")
      buildConfigField("boolean", "DISABLE_SCREENSHOT", "false")
      buildConfigField("boolean", "ALLOW_ROOTED_DEVICE", "true")
    }

    create("staging") {
      val stagingManifestEndpoint: String by project
      val stagingFallbackApiEndpoint: String by project
      val stagingMixpanelToken: String by project

      dimension = "track"
      applicationIdSuffix = ".staging"
      versionNameSuffix = "-demo"
      buildConfigField("String", "MANIFEST_ENDPOINT", "\"$stagingManifestEndpoint\"")
      buildConfigField("String", "FALLBACK_ENDPOINT", "\"$stagingFallbackApiEndpoint\"")
      buildConfigField("String", "MIXPANEL_TOKEN", "\"$stagingMixpanelToken\"")
      buildConfigField("boolean", "DISABLE_SCREENSHOT", "false")
      buildConfigField("boolean", "ALLOW_ROOTED_DEVICE", "true")
    }

    create("sandbox") {
      val sandboxManifestEndpoint: String by project
      val sandboxFallbackApiEndpoint: String by project
      val sandboxMixpanelToken: String by project

      dimension = "track"
      applicationIdSuffix = ".sandbox"
      versionNameSuffix = "-sandbox"
      buildConfigField("String", "MANIFEST_ENDPOINT", "\"$sandboxManifestEndpoint\"")
      buildConfigField("String", "FALLBACK_ENDPOINT", "\"$sandboxFallbackApiEndpoint\"")
      buildConfigField("String", "MIXPANEL_TOKEN", "\"$sandboxMixpanelToken\"")
      buildConfigField("boolean", "DISABLE_SCREENSHOT", "false")
      buildConfigField("boolean", "ALLOW_ROOTED_DEVICE", "true")
    }

    create("security") {
      val securityManifestEndpoint: String by project
      val securityFallbackApiEndpoint: String by project
      val securityMixpanelToken: String by project

      dimension = "track"
      applicationIdSuffix = ".security"
      versionNameSuffix = "-security"
      buildConfigField("String", "MANIFEST_ENDPOINT", "\"$securityManifestEndpoint\"")
      buildConfigField("String", "FALLBACK_ENDPOINT", "\"$securityFallbackApiEndpoint\"")
      buildConfigField("String", "MIXPANEL_TOKEN", "\"$securityMixpanelToken\"")
      buildConfigField("boolean", "DISABLE_SCREENSHOT", "true")
      buildConfigField("boolean", "ALLOW_ROOTED_DEVICE", "false")
    }

    create("production") {
      val productionManifestEndpoint: String by project
      val productionFallbackApiEndpoint: String by project
      val productionMixpanelToken: String by project

      dimension = "track"
      buildConfigField("String", "MANIFEST_ENDPOINT", "\"$productionManifestEndpoint\"")
      buildConfigField("String", "FALLBACK_ENDPOINT", "\"$productionFallbackApiEndpoint\"")
      buildConfigField("String", "MIXPANEL_TOKEN", "\"$productionMixpanelToken\"")
      buildConfigField("boolean", "DISABLE_SCREENSHOT", "true")
      buildConfigField("boolean", "ALLOW_ROOTED_DEVICE", "false")
    }
  }

  val filteredVariants = setOf(
      "qaRelease", "stagingDebug", "sandboxDebug", "productionDebug", "securityDebug"
  )
  variantFilter = Action<VariantFilter> {
    if (name in filteredVariants) {
      ignore = true
    }
  }

  lintOptions {
    lintConfig = file("${project.rootDir}/quality/lint.xml")
    isWarningsAsErrors = true
    isAbortOnError = true
    isCheckReleaseBuilds = false
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
    useIR = true
  }

  sourceSets {
    val sharedTestDir = "src/sharedTest/java"
    getByName("test") {
      java.srcDirs(sharedTestDir)
    }

    getByName("androidTest") {
      assets.srcDirs(files("$projectDir/schemas"))
      java.srcDirs(sharedTestDir)
    }

    getByName("main") {
      assets.srcDirs("${project.buildDir}/generated/assets/room_dao_metadata/")
    }
  }

  packagingOptions {
    // Deprecated ABIs. See https://developer.android.com/ndk/guides/abis
    exclude("lib/mips/libsqlite3x.so")
    exclude("lib/mips64/libsqlite3x.so")
    exclude("lib/armeabi/libsqlite3x.so")
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
        "kaptSandboxReleaseKotlin" to "sandboxRelease",
        "kaptStagingReleaseKotlin" to "stagingRelease",
        "kaptSecurityReleaseKotlin" to "securityRelease",
        "kaptProductionReleaseKotlin" to "productionRelease"
    )

    kaptTasks.forEach { (buildType, sourceSetName) ->
      val taskQualifier = buildType
          .replace("kapt", "")
          .replace("Kotlin", "")

      val taskName = "generate${taskQualifier}RoomMetadata"
      val generateTask = tasks.create<GenerateRoomMetadataTask>(taskName) {
        sourceSet.set(sourceSetName)
        csvAssetName.set("db_metadata.csv")
      }

      tasks.findByName(buildType)?.finalizedBy(generateTask)
      tasks.named("merge${taskQualifier}Assets").configure {
        dependsOn(generateTask)
      }
    }
  }
}

dependencies {
  implementation(project(":router"))
  implementation(project(":mobius-base"))
  implementation(project(":simple-platform"))
  implementation(project(":simple-visuals"))

  lintChecks(project(":lint"))

  testImplementation(project(":mobius-migration"))

  testImplementation("junit:junit:${versions.junit}")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${versions.mockitoKotlin}")
  testImplementation("pl.pragmatists:JUnitParams:${versions.junitParams}")
  testImplementation("com.google.truth:truth:${versions.truth}")
  testImplementation("com.github.blocoio:faker:${versions.faker}")
  testImplementation("com.spotify.mobius:mobius-test:${versions.mobius}")
  testImplementation("com.vinaysshenoy:quarantine-junit4:${versions.quarantine}")

  testRuntimeOnly("org.ow2.asm:asm:${versions.asm}") {
    because("not mandatory, but Truth recommends adding this dependency for better error reporting")
  }

  androidTestImplementation("androidx.annotation:annotation:${versions.supportLib}")

  androidTestImplementation("androidx.test:runner:${versions.androidXTest}")
  androidTestImplementation("androidx.test:rules:${versions.androidXTest}")
  androidTestImplementation("androidx.test.ext:junit:${versions.androidXTestExt}")

  androidTestImplementation("com.google.truth:truth:${versions.truth}")
  androidTestImplementation("com.github.blocoio:faker:${versions.faker}")
  androidTestImplementation("androidx.room:room-testing:${versions.room}")
  androidTestImplementation("androidx.arch.core:core-testing:${versions.coreTesting}") {
    // This dependency transitively pulls in a newer version of Mockito than Mockito-Kotlin does.
    // This results in the import statements in the unit tests breaking for mockito methods because
    // the IDE uses the version of Mockito that is present in the classpath from the androidTest
    // sourceset dependency, which raises an inspection error in the IDE. We can explicitly exclude
    // it from this dependency because it is also pulled in via mockito-kotlin.
    exclude(group = "org.mockito", module = "mockito-core")
  }
  androidTestImplementation("com.vinaysshenoy:quarantine-junit4:${versions.quarantine}") {
    exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
  }

  androidTestRuntimeOnly("org.ow2.asm:asm:${versions.asm}") {
    because("not mandatory, but Truth recommends adding this dependency for better error reporting")
  }

  kaptAndroidTest("com.google.dagger:dagger-compiler:${versions.dagger}")

  debugImplementation("com.github.blocoio:faker:${versions.faker}")
  debugImplementation("com.facebook.flipper:flipper:${versions.flipper}")
  debugImplementation("com.facebook.flipper:flipper-network-plugin:${versions.flipper}")
  debugImplementation("com.facebook.soloader:soloader:${versions.soloader}")
  debugImplementation("com.squareup.leakcanary:leakcanary-android:${versions.leakCanary}")

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${versions.kotlin}")

  implementation("androidx.recyclerview:recyclerview:${versions.recyclerView}")
  implementation("com.google.android.material:material:${versions.material}")
  implementation("androidx.cardview:cardview:${versions.cardview}")
  implementation("androidx.constraintlayout:constraintlayout:${versions.constraintLayout}")
  implementation("androidx.room:room-runtime:${versions.room}")
  kapt("androidx.room:room-compiler:${versions.room}")
  implementation("androidx.room:room-rxjava2:${versions.room}")
  implementation("com.google.android.gms:play-services-location:${versions.playServicesLocation}")
  implementation("com.google.firebase:firebase-config:${versions.firebaseConfig}")
  implementation("com.google.firebase:firebase-perf:${versions.firebasePerformance}")

  implementation("androidx.camera:camera-core:${versions.camerax}")
  implementation("androidx.camera:camera-camera2:${versions.camerax}")
  implementation("androidx.camera:camera-view:${versions.cameraView}")
  implementation("androidx.camera:camera-lifecycle:${versions.cameraLifecycle}")

  implementation("com.google.zxing:core:${versions.zxing}")

  implementation("androidx.paging:paging-runtime-ktx:${versions.paging}")
  implementation("androidx.paging:paging-rxjava2-ktx:${versions.paging}")

  implementation("com.google.dagger:dagger:${versions.dagger}")
  kapt("com.google.dagger:dagger-compiler:${versions.dagger}")

  implementation("io.reactivex.rxjava2:rxjava:${versions.rxJava}")
  implementation("io.reactivex.rxjava2:rxandroid:${versions.rxAndroid}")
  implementation("com.jakewharton.rxbinding2:rxbinding:${versions.rxBinding}")
  implementation("com.jakewharton.rxbinding2:rxbinding-recyclerview-v7:${versions.rxBinding}")
  implementation("com.jakewharton.rxbinding3:rxbinding:${versions.rxBinding3}")
  implementation("com.jakewharton.rxbinding3:rxbinding-recyclerview:${versions.rxBinding3}")
  implementation("com.jakewharton.rxbinding3:rxbinding-appcompat:${versions.rxBinding3}")
  implementation("io.reactivex.rxjava2:rxkotlin:${versions.rxKotlin}")
  implementation("com.squareup.retrofit2:retrofit:${versions.retrofit}")
  implementation("com.squareup.retrofit2:adapter-rxjava2:${versions.retrofit}")
  implementation("com.squareup.retrofit2:converter-moshi:${versions.retrofit}")
  implementation("com.squareup.retrofit2:converter-scalars:${versions.retrofit}")
  implementation("com.squareup.okhttp3:logging-interceptor:${versions.okLoggingInterceptor}")
  implementation("com.squareup.moshi:moshi:${versions.moshi}")
  kapt("com.squareup.moshi:moshi-kotlin-codegen:${versions.moshi}")
  implementation("com.squareup.moshi:moshi-adapters:${versions.moshi}")
  implementation("com.f2prateek.rx.preferences2:rx-preferences:${versions.rxPreference}")
  implementation("com.github.qoqa:Traceur:${versions.traceur}")
  implementation("ru.egslava:MaskedEditText:${versions.maskedEditText}")
  implementation("io.sentry:sentry-android:${versions.sentry}") {
    exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
  }

  implementation("com.mikepenz:itemanimators:${versions.itemAnimators}")
  implementation("org.mindrot:jbcrypt:${versions.jbcrypt}")
  implementation("com.squareup.flow:flow:${versions.flow}")
  implementation("io.requery:sqlite-android:${versions.sqliteAndroid}")

  implementation("com.google.android.gms:play-services-auth:${versions.playServicesAuth}")
  implementation("io.github.inflationx:viewpump:${versions.viewPump}")
  implementation("com.alimuzaffar.lib:pinentryedittext:${versions.pinEntryEditText}")
  implementation("info.debatty:java-string-similarity:${versions.javaStringSimilarity}")

  implementation("com.google.android.play:core:${versions.playCore}")

  implementation("com.simplecityapps:recyclerview-fastscroll:${versions.fastScroll}")
  implementation("com.mixpanel.android:mixpanel-android:${versions.mixpanel}")

  implementation("androidx.work:work-runtime:${versions.workManager}")
  implementation("androidx.work:work-gcm:${versions.workManager}")

  implementation("com.fasterxml.uuid:java-uuid-generator:${versions.uuidGenerator}")

  runtimeOnly("com.fasterxml.jackson.core:jackson-core:${versions.jackson}")
  implementation("ch.qos.logback:logback-classic:${versions.logback}")

  implementation("androidx.appcompat:appcompat:${versions.appcompat}")
  implementation("com.airbnb.android:lottie:${versions.lottie}")

  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${versions.desugarJdk}")

  implementation("com.github.gcacace:signature-pad:${versions.signaturePad}")

  implementation("androidx.viewpager2:viewpager2:${versions.viewpager2}")

  implementation("com.github.scottyab:rootbeer:${versions.rootbeer}")

  implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:${versions.mlKitBarcode}")

  implementation("androidx.fragment:fragment-ktx:${versions.fragment}")

  implementation("androidx.core:core-ktx:${versions.androidXCoreKtx}")

  implementation("org.threeten:threeten-extra:${versions.threetenExtra}")
}

// This must always be present at the bottom of this file, as per:
// https://console.firebase.google.com/u/2/project/simple-org/settings/general/
apply(plugin = "com.google.gms.google-services")

abstract class GenerateRoomMetadataTask : DefaultTask() {

  @get:Input
  abstract val sourceSet: Property<String>

  @get:Input
  abstract val csvAssetName: Property<String>

  @TaskAction
  fun run() {
    val rmg = RoomMetadataGenerator()
    val outputCsvPath = "${project.buildDir}/generated/assets/room_dao_metadata/${csvAssetName.get()}"
    rmg.run(project.projectDir.absolutePath, sourceSet.get(), outputCsvPath)
  }
}
