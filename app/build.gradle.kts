@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.tasks.databinding.DataBindingGenBaseClassesTask
import io.sentry.android.gradle.extensions.InstrumentationFeature
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool
import org.simple.rmg.RoomMetadataGenerator
import java.util.EnumSet

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.sentry)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.compose.compiler)
  alias(libs.plugins.google.services)
}

sentry {
  val sentryOrg: String by project
  val sentryProject: String by project
  val sentryAuthToken: String by project
  val sentryUploadProguard: String by project

  org = sentryOrg
  projectName = sentryProject
  authToken = sentryAuthToken

  includeProguardMapping.set(true)
  autoUploadProguardMapping.set(sentryUploadProguard.toBooleanStrict())

  // We are using our own instrumentation tooling for Room queries
  // Look at [ADR 013: SQL Performance Profiling (v2)]
  tracingInstrumentation {
    enabled = true
    features.set(EnumSet.allOf(InstrumentationFeature::class.java) - InstrumentationFeature.DATABASE)
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

  // Benchmark Gradle Plugin sets test build type as release, so it doesn't generate debug Android tasks.
  // Since we run Android tests on debug builds. We are setting it to debug.
  testBuildType = "debug"

  defaultConfig {
    applicationId = "org.simple.clinic"
    minSdk = minSdkVersion
    targetSdk = targetSdkVersion

    val versionCode = (project.properties["VERSION_CODE"] as? String)?.toInt() ?: 1
    val versionName = (project.properties["VERSION_NAME"] as? String) ?: "0.1"

    this.versionCode = versionCode
    this.versionName = versionName

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
    val manifestEndpoint: String by project
    val disableScreenshot: String by project
    val allowRootedDevice: String by project

    buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
    buildConfigField("String", "SENTRY_ENVIRONMENT", "\"$sentryEnvironment\"")
    buildConfigField("String", "MANIFEST_ENDPOINT", "\"$manifestEndpoint\"")
    buildConfigField("boolean", "DISABLE_SCREENSHOT", disableScreenshot)
    buildConfigField("boolean", "ALLOW_ROOTED_DEVICE", allowRootedDevice)

    ksp {
      arg("room.schemaLocation", "$projectDir/schemas")
      arg("room.incremental", "true")
      arg("room.expandProjection", "true")
    }
  }

  signingConfigs {
    create("release") {
      storeFile = file("$rootDir/release/simple.store")
      storePassword = "${project.properties["KEYSTORE_PASSWORD"]}"
      keyAlias = "${project.properties["KEY_ALIAS"]}"
      keyPassword = "${project.properties["KEY_PASSWORD"]}"
    }
  }

  buildTypes {
    getByName("debug") {
      applicationIdSuffix = ".debug"
      isMinifyEnabled = false
      isShrinkResources = false
    }

    val runProguard: String by project
    val maestroTests: String by project

    getByName("release") {
      isDebuggable = false
      isMinifyEnabled = runProguard.toBoolean()
      isShrinkResources = runProguard.toBoolean()
      signingConfig = if (maestroTests.toBoolean()) {
        getByName("debug").signingConfig
      } else {
        signingConfigs.getByName("release")
      }
    }
  }

  buildFeatures {
    viewBinding = true
    compose = true
    buildConfig = true
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
      variant.enable = variant.name !in filteredVariants
    }

    onVariants(selector().all()) { variant ->
      afterEvaluate {
        // This is a workaround for https://issuetracker.google.com/301245705 which depends on internal
        // implementations of the android gradle plugin and the ksp gradle plugin which might change in the future
        // in an unpredictable way.
        val variantName = variant.name.replaceFirstChar { it.titlecase() }
        project.tasks.getByName("ksp" + variantName + "Kotlin") {
          val dataBindingTask = project.tasks.getByName("dataBindingGenBaseClasses$variantName") as DataBindingGenBaseClassesTask
          (this as AbstractKotlinCompileTool<*>).setSource(dataBindingTask.sourceOutFolder)
        }
      }
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    isCoreLibraryDesugaringEnabled = true
  }

  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
    freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
  }

  sourceSets {
    getByName("androidTest") {
      assets.srcDirs(files("$projectDir/schemas"))
    }

    getByName("main") {
      assets.srcDirs("${project.layout.buildDirectory}/generated/assets/room_dao_metadata/")
    }
  }

  packaging {
    jniLibs {
      // Deprecated ABIs. See https://developer.android.com/ndk/guides/abis
      jniLibs.excludes.add("lib/mips/libsqlite3x.so")
      jniLibs.excludes.add("lib/mips64/libsqlite3x.so")
      jniLibs.excludes.add("lib/armeabi/libsqlite3x.so")
    }
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

  afterEvaluate {
    val kspTasks = mapOf(
        "kspQaDebugKotlin" to "qaDebug",
        "kspSandboxReleaseKotlin" to "sandboxRelease",
        "kspStagingReleaseKotlin" to "stagingRelease",
        "kspSecurityReleaseKotlin" to "securityRelease",
        "kspProductionReleaseKotlin" to "productionRelease"
    )

    kspTasks.forEach { (buildType, sourceSetName) ->
      val taskQualifier = buildType
          .replace("ksp", "")
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

  testFixtures {
    enable = true
  }
}

dependencies {
  /**
   * Debug dependencies
   */
  debugImplementation(libs.faker)
  debugImplementation(libs.leakcanary)
  debugImplementation(libs.chucker)

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
  implementation(libs.androidx.security.crypto)
  implementation(libs.androidx.activity)

  implementation(libs.bundles.androidx.camera)

  implementation(libs.bundles.androidx.paging)

  implementation(libs.bundles.androidx.room)
  ksp(libs.androidx.room.compiler)

  implementation(libs.bundles.androidx.work)

  implementation(libs.bundles.moshi)
  ksp(libs.moshi.codegen)

  implementation(libs.bundles.okhttp)

  implementation(libs.bundles.retrofit)

  implementation(libs.bundles.rx.binding)

  implementation(libs.dagger.dagger)
  ksp(libs.dagger.compiler)

  implementation(libs.edittext.masked)
  implementation(libs.edittext.pinentry)

  implementation(libs.firebase.config)
  implementation(libs.firebase.analytics)

  implementation(libs.itemanimators)

  implementation(libs.itext7)

  implementation(libs.jbcrypt)

  implementation(libs.kotlin.coroutines)
  implementation(libs.kotlin.coroutines.test)
  implementation(libs.kotlin.stdlib)

  implementation(libs.logback.classic)

  implementation(libs.lottie)

  implementation(libs.material)

  implementation(libs.okhttp.interceptor.logging)

  implementation(libs.openCsv) {
    exclude(module = "commons-logging")
  }

  implementation(libs.play.app.update)
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

  implementation(libs.rx.java.extensions)

  implementation(libs.uuid.generator)

  implementation(libs.viewpump)

  implementation(libs.zxing)

  implementation(libs.gson)

  implementation(projects.mobiusBase)
  implementation(projects.simplePlatform)
  implementation(projects.simpleVisuals)
  implementation(projects.commonUi)

  val composeBom = platform(libs.androidx.compose.bom)
  api(composeBom)
  androidTestImplementation(composeBom)
  api(libs.androidx.compose.material3)
  api(libs.androidx.compose.livedata)
  api(libs.androidx.compose.material.iconsExtended)
  api(libs.androidx.compose.ui.tooling.preview)
  debugImplementation(libs.androidx.compose.ui.tooling)

  implementation(libs.sqlCipher)

  /** Test fixtures dependencies **/
  testFixturesImplementation(libs.kotlin.stdlib)
  testFixturesImplementation(libs.junit)
  testFixturesImplementation(libs.faker)
  testFixturesImplementation(libs.rx.java.extensions)
  testFixturesImplementation(libs.rx.java)
  testFixturesImplementation(libs.retrofit.retrofit)
  testFixturesImplementation(libs.okhttp.okhttp)

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

  kspAndroidTest(libs.dagger.compiler)

  androidTestImplementation(libs.androidx.compose.test.junit)
  debugImplementation(libs.androidx.compose.test.manifest)

  androidTestImplementation(libs.androidx.paging.test)

  /**
   * Misc
   */
  coreLibraryDesugaring(libs.android.desugaring)

  lintChecks(projects.lint)

  runtimeOnly(libs.jackson.core)

  androidTestImplementation(libs.apache.commons.math)

  releaseImplementation(libs.chucker.no.op)
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

// Git hooks have to be manually copied and made executable. This task automates that.
val gitExecutableHooks: Task = tasks.create("gitExecutableHooks") {
  doLast {
    Runtime.getRuntime().exec("chmod -R +x .git/hooks/")
  }
}

val installGitHooks = tasks.create<Copy>("installGitHooks") {
  from(File("${rootProject.rootDir}/quality", "pre-push"))
  into(File(rootProject.rootDir, ".git/hooks"))
}

tasks.named("preBuild") {
  finalizedBy(installGitHooks)
}

gitExecutableHooks.dependsOn(installGitHooks)
tasks.named("clean") {
  dependsOn(gitExecutableHooks)
}

abstract class TransformGeneratedRoomDaoTask : DefaultTask() {

  private val projectDir = project.projectDir.absolutePath

  @get:Input
  abstract val sourceSet: Property<String>

  @get:Input
  abstract val reporterClassName: Property<String>


  @TaskAction
  fun run() {
    val rmg = RoomMetadataGenerator()
    rmg.run(projectDir, sourceSet.get(), reporterClassName.get())
  }
}
