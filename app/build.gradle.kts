// SPDX-License-Identifier: GPL-3.0-only

import com.android.build.api.artifact.SingleArtifact
import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import com.android.tools.r8.OutputMode

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.gerstudio.cleanbot"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.gerstudio.cleanbot"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.1"
    }

    androidResources {
        localeFilters += listOf("en", "es", "zh-rCN", "ja", "ru", "hi")
        generateLocaleConfig = true
    }

    val keystore = providers.environmentVariable("RELEASE_KEYSTORE").orNull
    signingConfigs {
        if (keystore != null) {
            create("release") {
                storeFile = file(keystore)
                val password = providers.environmentVariable("RELEASE_KEY_PASSWORD").get()
                storePassword = password
                keyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").get()
                keyPassword = password
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += setOf("META-INF/LICENSE.md", "META-INF/LICENSE-notice.md")
        }
    }

    buildFeatures {
        compose = true
    }

    sourceSets.getByName("test").java.srcDir("src/helper/java")

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.systemProperty("robolectric.defaultSdk", "35")
        }
    }
}

val compileLabelHelper = tasks.register<JavaCompile>("compileLabelHelper") {
    source = fileTree("src/helper/java")
    classpath = files(androidComponents.sdkComponents.bootClasspath)
    destinationDirectory = layout.buildDirectory.dir("intermediates/labelHelper/classes")
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

abstract class DexLabelHelper : DefaultTask() {
    @get:InputFiles abstract val classes: ConfigurableFileCollection
    @get:InputFiles abstract val androidJar: ConfigurableFileCollection
    @get:OutputDirectory abstract val outputDir: DirectoryProperty

    @TaskAction fun dex() {
        val out = outputDir.get().asFile.also { it.deleteRecursively(); it.mkdirs() }
        D8.run(
            D8Command.builder()
                .addProgramFiles(classes.asFileTree.matching { include("**/*.class") }.files.map { it.toPath() })
                .addLibraryFiles(androidJar.files.map { it.toPath() })
                .setMinApiLevel(21)
                .setOutput(out.resolve("label-helper.jar").toPath(), OutputMode.DexIndexed)
                .build(),
        )
    }
}

val dexLabelHelper = tasks.register<DexLabelHelper>("dexLabelHelper") {
    classes.from(compileLabelHelper)
    androidJar.from(androidComponents.sdkComponents.bootClasspath)
    outputDir = layout.buildDirectory.dir("generated/labelHelper/assets")
}

abstract class BundleChangelog : DefaultTask() {
    @get:InputFile abstract val changelog: RegularFileProperty
    @get:OutputDirectory abstract val outputDir: DirectoryProperty

    @TaskAction fun copy() {
        val out = outputDir.get().asFile.also { it.deleteRecursively(); it.mkdirs() }
        changelog.get().asFile.copyTo(out.resolve("CHANGELOG.md"))
    }
}

val bundleChangelog = tasks.register<BundleChangelog>("bundleChangelog") {
    changelog = rootProject.layout.projectDirectory.file("CHANGELOG.md")
    outputDir = layout.buildDirectory.dir("generated/changelog/assets")
}

abstract class CheckApkSize : DefaultTask() {
    @get:InputFiles abstract val apkDir: DirectoryProperty
    @get:Input abstract val maxBytes: Property<Long>

    @TaskAction fun check() {
        val apks = apkDir.get().asFile.listFiles { f -> f.extension == "apk" }.orEmpty()
        check(apks.isNotEmpty()) { "no APK in ${apkDir.get().asFile}" }
        for (apk in apks) {
            check(apk.length() <= maxBytes.get()) {
                "${apk.name} is ${apk.length()} bytes, over the ${maxBytes.get()}-byte budget"
            }
            logger.lifecycle("${apk.name}: ${apk.length()} of ${maxBytes.get()} bytes")
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(dexLabelHelper, DexLabelHelper::outputDir)
        variant.sources.assets?.addGeneratedSourceDirectory(bundleChangelog, BundleChangelog::outputDir)
    }
    onVariants(selector().withBuildType("release")) { variant ->
        val name = variant.name.replaceFirstChar { it.uppercase() }
        val sizeCheck = tasks.register<CheckApkSize>("check${name}ApkSize") {
            apkDir = variant.artifacts.get(SingleArtifact.APK)
            maxBytes = 3_500_000L
        }
        afterEvaluate { tasks.named("assemble$name") { finalizedBy(sizeCheck) } }
    }
}

dependencies {
    implementation(project(":adb"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.core)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.compose.icons.extended)
    implementation(libs.compose.material3.adaptive)
    implementation(libs.compose.material3.adaptive.layout)
    implementation(libs.compose.material3.adaptive.navigation)
    implementation(libs.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}
