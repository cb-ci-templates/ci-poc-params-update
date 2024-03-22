def values = ["one", "two", "three"]
pipeline {
    agent {
        kubernetes {
            yaml '''
                apiVersion: v1
                kind: Pod
                spec:
                  containers:
                  - name: shell
                    image: gradle:alpine
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
            environment {

            }
            steps {
                //  withCredentials([string(credentialsId: 'githubaccesstoken', variable: 'GH_ACCESS_TOKEN')]) {

                /*
                sh """
                        curl -L \
                          -H "Accept: application/vnd.github+json" \
                          -H "Authorization: Bearer ${GH_ACCESS_TOKEN}" \
                          -H "X-GitHub-Api-Version: 2022-11-28" \
                          https://api.github.com/repos/pipeline-demo-caternberg/pipeline-helloworld/branches
                    """

                 */
                echo sh(script: 'env|sort', returnStdout: true)
                jobDsl targets: ['updateParams.groovy'].join('\n'),
                        removedJobAction: 'DELETE',
                        removedViewAction: 'DELETE',
                        lookupStrategy: 'SEED_JOB',
                        additionalParameters: [params: "${values}"]
            }

        }
    }
}

