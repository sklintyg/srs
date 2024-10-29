import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import se.inera.intyg.srs.build.Config.Dependencies
import se.inera.intyg.srs.build.Config.Jvm
import se.inera.intyg.srs.build.Config.TestDependencies

plugins {
    kotlin("jvm")
    `kotlin-dsl`
    `maven-publish`

    id("io.spring.dependency-management")
}

allprojects {
    group = "se.inera.intyg.srs"
    version = System.getProperty("buildVersion", "0-SNAPSHOT")

    apply(plugin = "maven-publish")

    extra.apply {
        set("errorproneExclude", "true") //FIXME: Errorprone does not support Kotlin and KAPT. Until it does this will exclude the errorprone task for this project
        set("detekt", "true") // If '-P codeQuality' is set as a project property, this property activates the kotlin code analysis plugin Detekt
    }

    repositories {
        mavenLocal()
        maven {
            url = uri("https://nexus.drift.inera.se/repository/it-public/")
            mavenContent {
                releasesOnly()
            }
        }
        maven {
            url = uri("https://nexus.drift.inera.se/repository/it-public/")
            mavenContent {
                snapshotsOnly()
            }
        }
        mavenCentral()
    }

    publishing {
        repositories {
            maven {
                url = uri("https://nexus.drift.inera.se/repository/maven-releases/")
                credentials {
                    username = System.getProperty("ineraNexusUsername")
                    password = System.getProperty("ineraNexusPassword")
                }
            }
        }
    }
}

subprojects {
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "kotlin")

    apply<DependencyManagementPlugin>()

    dependencyManagement {
        imports {
            mavenBom("org.springframework:spring-framework-bom:${Dependencies.springVersion}")
            mavenBom("org.springframework.boot:spring-boot-dependencies:${Dependencies.springBootVersion}") {
                bomProperty("kotlin.version", Dependencies.kotlinVersion)
            }
            mavenBom("org.junit:junit-bom:${TestDependencies.junit5Version}")
        }
    }

    dependencies {
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

        compileOnly("com.github.spotbugs:spotbugs-annotations:${Dependencies.spotbugsAnnotationsVersion}")
        // spotbugs-annotations Example usage: @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")

        implementation("jakarta.jws:jakarta.jws-api:${Dependencies.jakartaJwsVersion}")
        implementation("com.sun.activation:javax.activation:1.2.0")
        implementation("javax.xml.ws:jaxws-api:${Dependencies.jaxWsVersion}")

        implementation("org.apache.commons:commons-lang3")
        implementation("com.google.guava:guava:${Dependencies.guavaVersion}")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("org.junit.platform:junit-platform-runner") {
            exclude(module = "junit")
        }
        testImplementation("org.mockito:mockito-core:${TestDependencies.mockitoCoreVersion}")
        testImplementation("org.mockito:mockito-junit-jupiter:${TestDependencies.mockitoCoreVersion}")

        testImplementation(kotlin("test"))
    }

    tasks {

        withType<Test> {
            useJUnitPlatform()
            jvmArgs = listOf("-Djava.library.path=/usr/local/lib/R/3.6/site-library/rJava/jri")
        }

        withType<JavaCompile> {
            sourceCompatibility = Jvm.sourceCompatibility
            targetCompatibility = Jvm.targetCompatibility
            options.encoding = Jvm.encoding
        }

        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = Jvm.kotlinJvmTarget
        }
    }
}