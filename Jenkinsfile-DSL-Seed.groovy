def values = "one, two, three"
pipeline {
    agent {
        kubernetes {
            yaml '''
                apiVersion: v1
                kind: Pod
                spec:
                  containers:
                  - name: shell
                    image: curlimages/curl:latest
                    command:
                    - sleep
                    args:
                    - infinity
                '''
            defaultContainer 'shell'
        }
    }
    stages {
        stage('SeedDSL') {

            steps {
                withCredentials([string(credentialsId: 'github-token', variable: 'GH_ACCESS_TOKEN')]) {
                   echo "$GH_ACCESS_TOKEN"
                    sh """
                           curl -Lv \
                          -H 'Accept: application/vnd.github+json' \
                          -H 'Authorization: Bearer ${GH_ACCESS_TOKEN}' \
                          -H 'X-GitHub-Api-Version: 2022-11-28' \
                          https://api.github.com/repos/pipeline-demo-caternberg/pipeline-helloworld/branches
                     """
                   /* script {
                         def branches = sh(script: """
                                    curl -L \
                                      -H "Accept: application/vnd.github+json" \
                                      -H "Authorization: Bearer ${GH_ACCESS_TOKEN}" \
                                      -H "X-GitHub-Api-Version: 2022-11-28" \
                                      https://api.github.com/repos/pipeline-demo-caternberg/pipeline-helloworld/branches
                                """, returnStatus: true)
                        println branches
                    }
                    */
                }
                //echo sh(script: 'env|sort', returnStdout: true)
                jobDsl targets: ['updateParams.groovy'].join('\n'),
                        removedJobAction: 'DELETE',
                        removedViewAction: 'DELETE',
                        lookupStrategy: 'SEED_JOB',
                        additionalParameters: [params: "${values}"]
            }
        }
    }
}

