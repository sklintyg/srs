package se.inera.intyg.srs.build

object Config {

    object Jvm {
        const val sourceCompatibility = "21"
        const val targetCompatibility = "21"
        const val kotlinJvmTarget = "21"
        const val encoding = "UTF-8"
    }

    object Dependencies {

        //Project dependencies
        const val srsSchemasVersion = "0.0.16.2"
        const val monitoringSchemasVersion = "1.0.0.5.2"

        //External dependencies

        const val kotlinVersion = "2.0.20"

        const val springVersion = "6.1.0"
        const val springBootVersion = "3.3.4"
        const val springDependencyManagementVersion = "1.1.6"

        const val cxfBootStarterVersion = "4.0.5"
        const val guavaVersion = "32.1.3-jre"
        const val liquibaseVersion = "4.27.0"
        const val jaxWsVersion = "2.3.1"
        const val jakartaJwsVersion = "3.0.0"

        // Spotbugs annotations
        const val spotbugsAnnotationsVersion = "4.8.6"
    }

    object TestDependencies {
        const val mockitoCoreVersion = "5.14.2"
        const val junit5Version = "5.9.2"
        const val restAssuredVersion = "5.2.0"
    }
}
