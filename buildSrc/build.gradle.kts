import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.41"
}

repositories {
  gradlePluginPortal()
  mavenLocal()
  maven("https://nexus.drift.inera.se/repository/maven-releases/")
  mavenCentral()

}

//dependencies {
  //implementation(kotlin("stdlib-jdk8"))
//}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
  jvmTarget = "11"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
  jvmTarget = "11"
}
