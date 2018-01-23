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
            publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/allTests', \
                reportFiles: 'index.html', reportName: 'JUnit results'
        }
    }
}


stage('deploy') {
    node {
        util.run {
            ansiblePlaybook extraVars: [version: buildVersion, ansible_ssh_port: "22", deploy_from_repo: "false"], \
                installation: 'ansible-yum', inventory: 'ansible/inventory/srs/test', playbook: 'ansible/site.yml'
        }
    }
}

stage('restAssured') {
    node {
        try {
            shgradle "restAssuredTest -DbaseUrl=http://srs.inera.nordicmedtest.se/"
        } finally {
            publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/tests/restAssuredTest', \
                reportFiles: 'index.html', reportName: 'RestAssured results'
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
