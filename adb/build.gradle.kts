// SPDX-License-Identifier: GPL-3.0-only

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

configurations.all {
    exclude(group = "org.lsposed.hiddenapibypass", module = "hiddenapibypass")
    exclude(group = "me.weishu", module = "free_reflection")
    exclude(group = "com.flyfishxu", module = "kadb")
    exclude(group = "com.flyfishxu", module = "kadb-mdns")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.okio)
    api(libs.bcprov)
    api(libs.bcpkix)
    api(libs.spake2)

    compileOnly(libs.conscrypt.openjdk.uber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.conscrypt.openjdk.uber)
}
