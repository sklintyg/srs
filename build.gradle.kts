import org.gradle.api.publish.maven.MavenPublication
import se.inera.intyg.TagReleaseTask
import se.inera.intyg.VersionPropertyFileTask

plugins {
    war
    `maven-publish`

    kotlin("jvm") version "1.2.21"
    kotlin("plugin.spring") version "1.2.21"
    kotlin("plugin.jpa") version "1.2.21"

    id("se.inera.intyg.plugin.common") version "1.0.62"
    id("org.springframework.boot") version "1.5.6.RELEASE"
    id("org.ajoberstar.grgit") version "2.0.0"
//    id("org.jlleitschuh.gradle.ktlint") version "3.0.0"
}

group = "se.inera.intyg.srs"
version = System.getProperty("buildVersion") ?: "0.0.1-SNAPSHOT"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions { jvmTarget = "1.8" }
}

val versionTask = task<VersionPropertyFileTask>("createVersionPropertyFile")
tasks["war"].dependsOn(versionTask)

task<TagReleaseTask>("tagRelease")

tasks.withType<Test> {
    exclude("**/*IT*")
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
            url = uri("https://build-inera.nordicmedtest.se/nexus/repository/releases/")
            credentials {
                username = System.getProperty("nexusUsername")
                password = System.getProperty("nexusPassword")
            }
        }
    }
}

dependencies {
    val kotlinVersion = "1.2.21"

    compile(kotlin("stdlib", kotlinVersion))
    compile(kotlin("reflect", kotlinVersion))

    compile("se.inera.intyg.clinicalprocess.healthcond.srs:intyg-clinicalprocess-healthcond-srs-schemas:0.0.7")
    compile("se.riv.itintegration.monitoring:itintegration-monitoring-schemas:1.0.0.4")

    listOf("org.springframework.boot:spring-boot-starter-web",
    "org.springframework.boot:spring-boot-starter-jdbc:",
    "org.springframework.boot:spring-boot-starter-data-jpa",
    "org.springframework.boot:spring-boot-starter-web-services",
    "org.springframework.boot:spring-boot-starter-actuator",
    "org.springframework.boot:spring-boot-starter-log4j2",
    "org.apache.cxf:cxf-spring-boot-starter-jaxws:3.1.11")
            .forEach {
                compile(it) {
                    exclude(module = "spring-boot-starter-logging")
                    exclude(module = "logback-classic")
                }
            }

    compile("org.springframework.boot:spring-boot-starter-log4j2")

    compile("org.liquibase:liquibase-core:3.5.3")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.4")
    compile("org.nuiton.thirdparty:JRI:0.9-9")
    compile("org.jadira.usertype:usertype.extended:5.0.0.GA")
    compile("com.google.guava:guava:23.0")

    runtime("com.h2database:h2")
    runtime("mysql:mysql-connector-java")

    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("com.jayway.restassured:rest-assured:2.8.0")
    testCompile("com.nhaarman:mockito-kotlin-kt1.1:1.5.0")
    testCompile("org.exparity:hamcrest-date:2.0.1")
}


repositories {
    mavenLocal()
    maven("https://build-inera.nordicmedtest.se/nexus/repository/releases/")
    mavenCentral()
}

