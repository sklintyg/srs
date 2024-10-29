import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "2.0.20"
}

repositories {
  gradlePluginPortal()
  mavenLocal()
  mavenCentral()
  maven("https://nexus.drift.inera.se/repository/it-public/")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
  jvmTarget = "21"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
  jvmTarget = "21"
}