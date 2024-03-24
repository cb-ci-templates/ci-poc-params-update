library identifier: 'ci-shared-library@main', retriever: modernSCM(
        [$class: 'GitSCMSource',
         remote: 'https://github.com/cb-ci-templates/ci-shared-library.git'])
//def branches = "one, two, three"
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
        REPO_BRANCH = "https://api.github.com/repos/org-caternberg/dsl-params-update/branches"
        //GIT_REMOTE_BRANCHES = "one, two, three" // For testing purpose
    }
    stages {
        stage('UpdateParams') {
            steps {
                container("shell") {
                    jobDsl targets: ['jobdsl_updateJobParams.groovy'].join('\n'),
                            removedJobAction: 'DELETE',
                            removedViewAction: 'DELETE',
                            lookupStrategy: 'SEED_JOB',
                            additionalParameters: [params: getGitBranches("$GH_ACCESS_TOKEN","$REPO_BRANCH") ]
                }
            }
        }
    }
}

