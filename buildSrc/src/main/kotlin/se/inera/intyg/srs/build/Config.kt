package se.inera.intyg.srs.build

object Config {

    object Jvm {
        const val sourceCompatibility = "1.8"
        const val targetCompatibility = "1.8"
        const val kotlinJvmTarget = "1.8"
        const val encoding = "UTF-8"
    }

    object Dependencies {

        //Project dependencies
        const val intygPluginVersion = "3.0.6"
        const val srsSchemasVersion = "0.0.14-SNAPSHOT"
        const val monitoringSchemasVersion = "1.0.0.5"

        //External dependencies

        const val kotlinVersion = "1.3.31"

        const val springVersion = "5.1.7.RELEASE"
        const val springBootVersion = "2.1.5.RELEASE"
        const val springDependencyManagementVersion = "1.0.7.RELEASE"

        const val cxfBootStarterVersion = "3.3.3"
        const val guavaVersion = "27.1-jre"
        const val liquibaseVersion = "3.6.3"
        const val jaxWsVersion = "2.3.0"
        const val jakartaJwsVersion = "1.1.1"

        // Spotbugs annotations
        const val spotbugsAnnotationsVersion = "3.1.12"
    }

    object TestDependencies {
        const val mockitoCoreVersion = "2.27.0"
        const val junit5Version = "5.4.2"
        const val restAssuredVersion = "4.0.0"
    }
}
