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
                    image: caternberg/curl-yq-jq
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
        JENKINS_TOKEN = credentials("jenkins-token")
        GH_API_REPO_BRANCH = "https://api.github.com/repos/cb-ci-templates/ci-poc-params-update/branches"
        //GIT_REMOTE_BRANCHES = "[one, two, three]" // For testing purpose
    }
    stages {
        stage('UpdateParams') {
            steps {
                container("shell") {
                    dir("resources") {
                        //Shared Lib function collects al remote branches and exposes to env.GIT_REPO_BRANCHES}
                        getGitBranches("$GH_ACCESS_TOKEN", "$GH_API_REPO_BRANCH")
                        sh(script: "./casc-updateJobParams.sh ${JENKINS_TOKEN} [${env.GIT_REPO_BRANCHES}]", returnStatus: true)
                    }
                }
            }
        }
    }
}

