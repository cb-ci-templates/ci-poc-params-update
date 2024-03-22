def branches = "one, two, three"
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

    stages {
        stage('SeedDSL') {
            steps {
                container("shell") {
                    environment {
                        GH_ACCESS_TOKEN = credentials("github-token")
                        REPO_BRANCH = "https://api.github.com/repos/org-caternberg/dsl-params-update/branches"
                        BRANCHES=sh(script: "./script-curl-branches.sh $GH_ACCESS_TOKEN  $REPO_BRANCH  |jq -r '.[] | .name' | tr '\\n' ', ' | sed 's/,\$//'", returnStatus: true).trim()
                    }
  /*                  script {
                        branches=sh(script: "./script-curl-branches.sh $GH_ACCESS_TOKEN  $REPO_BRANCH  |jq -r '.[] | .name' | tr '\\n' ', ' | sed 's/,\$//'", returnStatus: true).trim()
                        println "BRANCHES: $branches"
                    }*/
                    //echo sh(script: 'env|sort', returnStdout: true)
                    jobDsl targets: ['updateParams.groovy'].join('\n'),
                            removedJobAction: 'DELETE',
                            removedViewAction: 'DELETE',
                            lookupStrategy: 'SEED_JOB',
                            additionalParameters: [params: "${BRANCHES}"]
                }
            }
        }
    }
}

