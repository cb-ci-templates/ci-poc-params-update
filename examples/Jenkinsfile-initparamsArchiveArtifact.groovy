package examples
// Uses Declarative syntax to run commands inside a container.
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
    stages {
        stage('Main') {
            steps {
                echo "This job create some sample values as input for the examples.Jenkinsfile-ActiveChoice-readParamsFromOtherArchiveArtifatc Job"
                sh 'echo "one\ntwo\nthree\n" > newparams.txt'
                sh "cat newparams.txt"
                archiveArtifacts artifacts: 'newparams.txt', fingerprint: true, followSymlinks: false
            }
        }
    }
}
