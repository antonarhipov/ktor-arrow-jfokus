val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val arrow_version: String by project
val arrow_meta_version: String by project
val arrow_analysis_version: String by project

plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.3"
    id("com.google.devtools.ksp") version "1.8.10-1.0.9"
}

group = "com.example"
version = "0.0.1"
application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

//buildscript {
//    dependencies {
//        classpath("io.arrow-kt.analysis.kotlin:io.arrow-kt.analysis.kotlin.gradle.plugin:2.0.3-alpha.2")
//    }
//}
//apply(plugin = "io.arrow-kt.analysis.kotlin")

dependencies {
    ksp("io.arrow-kt:arrow-optics-ksp-plugin:$arrow_version")
    implementation("io.arrow-kt:arrow-core:$arrow_version")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrow_version")
    implementation("io.arrow-kt:arrow-optics:$arrow_version")

    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auto-head-response:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-gson-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
//            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers",
//            "-Xuse-k2",
            "-Xbackend-threads=4",
        )
        jvmTarget = "11"
        languageVersion = "1.8"
    }
}
