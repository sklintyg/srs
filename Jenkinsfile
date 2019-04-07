#!groovy

def buildVersion = "1.2.${BUILD_NUMBER}"

stage('checkout') {
    node {
        git url: "https://github.com/sklintyg/SRS.git", branch: GIT_BRANCH
        util.run { checkout scm }
    }
}

stage('build') {
    node {
        try {
            shgradle "--refresh-dependencies clean build -DbuildVersion=${buildVersion}"
        } finally {
            publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'web/build/reports/allTests', \
                reportFiles: 'index.html', reportName: 'JUnit results'
        }
    }
}

stage('tag and upload') {
    node {
        shgradle "publish tagRelease -DbuildVersion=${buildVersion}"
    }
}

stage('notify') {
    node {
        util.notifySuccess()
    }
}


stage('propagate') {
    node {
        gitRef = "v${buildVersion}"
        releaseFlag = "${GIT_BRANCH.startsWith("release")}"
        build job: "webcert-dintyg-build", wait: false, parameters: [
                [$class: 'StringParameterValue', name: 'BUILD_VERSION', value: buildVersion],
                [$class: 'StringParameterValue', name: 'GIT_REF', value: gitRef],
                [$class: 'StringParameterValue', name: 'RELEASE_FLAG', value: releaseFlag]
        ]
    }
}
