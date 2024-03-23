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
    }
    stages {
        stage('SeedDSL') {
            steps {
                container("shell") {
                    jobDsl targets: ['resources/updateParams.groovy'].join('\n'),
                            removedJobAction: 'DELETE',
                            removedViewAction: 'DELETE',
                            lookupStrategy: 'SEED_JOB',
                            additionalParameters: [params: getGitBranches("$GH_ACCESS_TOKEN","$REPO_BRANCH") ]
                }
            }
        }
    }
}

