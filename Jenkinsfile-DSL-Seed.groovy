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
                    image: caternberg/jenkins-agent-customized:latest
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
        REPO_BRANCH = "https://api.github.com/repos/org-caternberg/dsl-params-update/branches"
    }
    stages {
        stage('SeedDSL') {
            steps {
                script {
                     def branches = sh(script: "script-curl-branches.sh $GH_ACCESS_TOKEN  $REPO_BRANCH", returnStatus: true)
                     println branches
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

