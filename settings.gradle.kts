pluginManagement {
    val kotlinVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val springBootVersion: String by settings
    repositories {
        maven("https://nexus.drift.inera.se/repository/it-public/")
        gradlePluginPortal()
        mavenLocal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin.")) {
                useVersion(kotlinVersion)
            }
            if (requested.id.id.startsWith("io.spring.dependency-management")) {
                useVersion(springDependencyManagementVersion)
            }
            if (requested.id.id.startsWith("org.springframework.boot")) {
                useVersion(springBootVersion)
            }
        }
    }
}

rootProject.name = "srs"

//include(":common")
//include(":integration")
//include(":persistence")
include(":web")

fun getProjectDirName(project: String): String {
    return when(project) {
//        "${rootProject.name}-common" ->"$rootDir/common"
//        "${rootProject.name}-integration" ->"$rootDir/integration"
//        "${rootProject.name}-persistence" ->"$rootDir/persistence"
        "${rootProject.name}-web" ->"$rootDir/web"
        else -> throw IllegalArgumentException("Project module $project does not exist.")
    }
}

for (project in rootProject.children) {
    project.name = "${rootProject.name}-${project.name}"
    val projectName = project.name

    project.projectDir = file(getProjectDirName(projectName))
    project.buildFileName = "build.gradle.kts"

    if (!project.projectDir.isDirectory) {
        throw IllegalArgumentException("Project directory ${project.projectDir} for project ${project.name} does not exist.")
    }

    if (!project.buildFile.isFile) {
        throw IllegalArgumentException("Build file ${project.buildFile} for project ${project.name} does not exist.")
    }
}
