import org.gradle.api.publish.maven.MavenPublication
import org.springframework.boot.gradle.tasks.bundling.BootWar
import se.inera.intyg.TagReleaseTask
import se.inera.intyg.VersionPropertyFileTask

plugins {
    war
    `maven-publish`

    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "6.1.0"
    kotlin("plugin.jpa") version "1.3.21"

    id("io.spring.dependency-management") version "1.1.6"
    id("org.springframework.boot") version "3.3.4"
    id("org.ajoberstar.grgit") version "2.0.0"
    // FIXME: doesn't work anymore
    //id("org.jlleitschuh.gradle.ktlint") version "3.0.0"
}

// harmonize output dir with openshift pipeline (convention)
rootProject.buildDir = file("${rootDir}/web/build")

group = "se.inera.intyg.srs"
version = System.getProperty("buildVersion") ?: "0.0.1-SNAPSHOT"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions { jvmTarget = "21" }
}

val versionTask = task<VersionPropertyFileTask>("createVersionPropertyFile")
tasks["war"].dependsOn(versionTask)

task<TagReleaseTask>("tagRelease")

tasks.withType<Test> {
    exclude("**/*IT*")
}

tasks.getByName<BootWar>("bootWar"){
    manifest {
        attributes("Main-Class" to "org.springframework.boot.loader.PropertiesLauncher")
        attributes("Start-Class" to "se.inera.intyg.srs.ApplicationKt")
    }
}

task<Test>("restAssuredTest") {
    outputs.upToDateWhen { false }
    systemProperty("integration.tests.baseUrl", System.getProperty("baseUrl") ?: "http://localhost:8080/")
    include("**/*IT*")
    excludes.clear()
}


publishing {
    publications {
        create<MavenPublication>("warFile") {
            from(components["web"])
        }
    }
    repositories {
        maven {
            url = maven("https://nexus.drift.inera.se/repository/it-public/")
            credentials {
                username = System.getProperty("nexusUsername")
                password = System.getProperty("nexusPassword")
            }
        }
    }
}

dependencies {
    val kotlinVersion = "2.0.20"

    compile(kotlin("stdlib", kotlinVersion))
    compile(kotlin("reflect", kotlinVersion))

    compile("se.inera.intyg.clinicalprocess.healthcond.srs:intyg-clinicalprocess-healthcond-srs-schemas:0.0.13")
    compile("se.riv.itintegration.monitoring:itintegration-monitoring-schemas:1.0.0.5")

    // External dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("org.apache.cxf:cxf-spring-boot-starter-jaxws:3.2.5")

    compile("org.liquibase:liquibase-core:4.27.0")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
    compile("org.nuiton.thirdparty:JRI:0.9-9")
    compile("com.google.guava:guava:32.1.3-jre")
    compile("org.apache.poi:poi-ooxml:4.0.1")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("mysql:mysql-connector-java:8.0.32")

    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("com.jayway.restassured:rest-assured:2.8.0")
    testCompile("com.nhaarman:mockito-kotlin-kt1.1:1.5.0")
    testCompile("org.exparity:hamcrest-date:2.0.5")
}

repositories {
    mavenLocal()
    maven("https://build-inera.nordicmedtest.se/nexus/repository/releases/")
    mavenCentral()
}

