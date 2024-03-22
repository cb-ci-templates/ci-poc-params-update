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
                    image: ubuntu
                    command:
                    - sleep
                    args:
                    - infinity
                '''
            defaultContainer 'shell'
        }
    }
    environment {
        GH_ACCESS_TOKEN = credentials("github-token")
        REPO_BRANCH = "https://api.github.com/repos/pipeline-demo-caternberg/pipeline-helloworld/branches"
    }
    stages {
        stage('SeedDSL') {
            steps {
                echo  "curl -Lv -H 'Accept: application/vnd.github+json' -H 'Authorization: Bearer ${GH_ACCESS_TOKEN}'  -H 'X-GitHub-Api-Version: 2022-11-28'  $REPO_BRANCH"
                /* script {
                      def branches = sh(script: """
                                 curl -L \
                                   -H "Accept: application/vnd.github+json" \
                                   -H "Authorization: Bearer ${GH_ACCESS_TOKEN}" \
                                   -H "X-GitHub-Api-Version: 2022-11-28" \
                                   $REPO_BRANCH
                             """, returnStatus: true)
                     println branches
                 }
                 */
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

