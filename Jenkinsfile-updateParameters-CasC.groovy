library identifier: 'ci-shared-library@main', retriever: modernSCM(
        [$class: 'GitSCMSource',
         remote: 'https://github.com/cb-ci-templates/ci-shared-library.git'])

pipeline {
    agent {
        kubernetes {
            yaml '''
                apiVersion: v1
                kind: Pod
                spec:
                  containers:
                  - name: shell
                    image: softonic/curl-jq:3.18.2-1
                    command:
                      - cat
                    tty: true
                    workingDir: "/home/jenkins/agent"
                    securityContext:
                      runAsUser: 1000
                '''
            defaultContainer 'shell'
        }
    }
    environment {
        GH_ACCESS_TOKEN = credentials("github-token")
        REPO_BRANCH = "https://api.github.com/repos/org-caternberg/dsl-params-update/branches"
        //GIT_REMOTE_BRANCHES = "one, two, three" // For testing purpose
    }
    stages {
        stage('SeedDSL') {
            steps {
                container("shell") {
                    withCredentials([usernameColonPassword(credentialsId: 'jenkins-token', variable: 'JENKINS_TOKEN')]) {
                        dir ("resources"){
                            //Shared Lib collects al remote branches and exposes to env.GIT_REPO_BRANCHES}
                            getGitBranches("$GH_ACCESS_TOKEN","$REPO_BRANCH")
                            sh "script:casc-updateJobParams.sh ${JENKINS_TOKEN} ${env.GIT_REPO_BRANCHES}"
                        }
                    }
                }
            }
        }
    }
}

