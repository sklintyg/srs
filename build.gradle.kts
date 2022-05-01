import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import se.inera.intyg.IntygPluginCheckstyleExtension
import se.inera.intyg.JavaVersion
import se.inera.intyg.TagReleaseTask
import se.inera.intyg.srs.build.Config.Dependencies
import se.inera.intyg.srs.build.Config.Jvm
import se.inera.intyg.srs.build.Config.TestDependencies

plugins {
    kotlin("jvm")
    `kotlin-dsl`
    maven
    `maven-publish`

    id("se.inera.intyg.plugin.common") apply false
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
        maven ("https://nexus.drift.inera.se/repository/it-public/")
        mavenCentral {
            content {
                // this repository contains everything BUT artifacts with group starting with "se.inera"
                excludeGroupByRegex("se\\.inera.*")
            }
        }
    }

    publishing {
        repositories {
            maven {
                url = uri("https://nexus.drift.inera.se/repository/maven-releases/")
                credentials {
                    username = System.getProperty("nexusUsername")
                    password = System.getProperty("nexusPassword")
                }
            }
        }
    }
}

apply(plugin = "se.inera.intyg.plugin.common")

subprojects {
    apply(plugin = "org.gradle.maven")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "se.inera.intyg.plugin.common")
    apply(plugin = "kotlin")

    apply<DependencyManagementPlugin>()

    configure<IntygPluginCheckstyleExtension> {
        javaVersion = JavaVersion.JAVA11
        showViolations = true
        ignoreFailures = false
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${Dependencies.springBootVersion}")
            mavenBom("org.junit:junit-bom:${TestDependencies.junit5Version}")
        }
        dependencies {
            dependency ("org.springframework:spring-web:${Dependencies.springVersion}")
            dependency ("org.springframework:spring-webmvc:${Dependencies.springVersion}")
            dependency ("org.springframework:spring-aop:${Dependencies.springVersion}")
            dependency ("org.springframework:spring-beans:${Dependencies.springVersion}")
            dependency ("org.springframework:spring-context:${Dependencies.springVersion}")
            dependency ("org.springframework:spring-core:${Dependencies.springVersion}")
            dependency ("org.springframework:spring-orm:${Dependencies.springVersion}")
            dependency ("org.springframework:spring-tx:${Dependencies.springVersion}")
            dependency ("org.springframework:spring-aspects:${Dependencies.springVersion}")
            dependency ("org.springframework:spring-jdbc:${Dependencies.springVersion}")

            dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Dependencies.kotlinVersion}")
            dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Dependencies.kotlinVersion}")
            dependency("org.jetbrains.kotlin:kotlin-stdlib:${Dependencies.kotlinVersion}")
            dependency("org.jetbrains.kotlin:kotlin-stdlib-common:${Dependencies.kotlinVersion}")
            dependency("org.jetbrains.kotlin:kotlin-test:${Dependencies.kotlinVersion}")
            dependency("org.jetbrains.kotlin:kotlin-test-common:${Dependencies.kotlinVersion}")
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
        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testImplementation("org.junit.platform:junit-platform-runner") {
            exclude(module = "junit")
        }
        testImplementation("org.mockito:mockito-core:${TestDependencies.mockitoCoreVersion}")
        // mockito-inline: To be able to mock final classes
        testImplementation ("org.mockito:mockito-inline:${TestDependencies.mockitoCoreVersion}")
        testImplementation("org.mockito:mockito-junit-jupiter:${TestDependencies.mockitoCoreVersion}")

        testImplementation(kotlin("test"))

        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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

tasks {
    register<TagReleaseTask>("tagRelease")
}

dependencies {
    subprojects.forEach { archives(it) }
}