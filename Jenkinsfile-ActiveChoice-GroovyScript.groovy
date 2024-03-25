properties([parameters(
        [activeChoice(choiceType: 'PT_SINGLE_SELECT',
                filterLength: 1, filterable: false,
                name: 'mychoice',
                randomName: 'choice-parameter-2381968702490339',
                script: groovyScript(
                        fallbackScript: [classpath: [],
                                         oldScript: '',
                                         sandbox  : false, script: 'return  false'],
                                script: [classpath: [],
                                         oldScript: '',
                                         sandbox  : false, script: '''
                                                    def CREDENTIAL_ID = "gh-token-ci-templates-repo-classic"
                                                    def SECRET = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getStore().getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getSecret().getPlainText()
                                                    def URL = "https://" + SECRET + "@github.com/cb-ci-templates/ci-poc-params-update.git"
                                                    def result = ["/bin/bash", "-c", "git ls-remote -h " + URL + " | sed 's/.*refs\\\\/heads\\\\/\\\\(.*\\\\)/\\\\1/'"].execute().text.tokenize();
                                                    return result                                       
                                    ''']
                                )
                    )
        ]
),
            [$class: 'JobLocalConfiguration', changeReasonComment: '']])
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
    stages {
        stage('PrintParam') {
            steps {
                container("shell") {
                    echo "Hello ${mychoice}"
                }
            }
        }
    }
}
