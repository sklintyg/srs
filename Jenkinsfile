def buildVersion = "1.0.${BUILD_NUMBER}"
def schemasVersion = "0.0.+"

stage('checkout') {
    node {
        git url: "https://github.com/sklintyg/SRS.git", branch: GIT_BRANCH
        util.run { checkout scm }
    }
}

stage('build') {
    node {
        try {
            shgradle "--refresh-dependencies clean build -DbuildVersion=${buildVersion} -DschemasVersion=${schemasVersion}"
        } finally {
            publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/allTests', \
                reportFiles: 'index.html', reportName: 'JUnit results'
        }
    }
}

// TODO: Uncomment when time has come
//stage('deploy') {
    //node {
        //util.run {
            //ansiblePlaybook extraVars: [version: buildVersion, ansible_ssh_port: "22", deploy_from_repo: "false"], \
                //installation: 'ansible-yum', inventory: 'ansible/inventory/srs/test', playbook: 'ansible/deploy.yml'
            //util.waitForServer('https://srs.inera.nordicmedtest.se/inera-certificate/version.jsp')
        //}
    //}
//}

// TODO: Uncomment when time has come
//stage('restAssured') {
    //node {
        //try {
            //shgradle "restAssuredTest -DbaseUrl=http://srs.inera.nordicmedtest.se/"
        //} finally {
            //publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'web/build/reports/tests/restAssuredTest', \
                //reportFiles: 'index.html', reportName: 'RestAssured results'
        //}
    //}
//}

stage('tag and upload') {
    node {
        shgradle "uploadArchives tagRelease -DbuildVersion=${buildVersion} -DschemasVersion=${schemasVersion}"
    }
}

stage('notify') {
    node {
        util.notifySuccess()
    }
}