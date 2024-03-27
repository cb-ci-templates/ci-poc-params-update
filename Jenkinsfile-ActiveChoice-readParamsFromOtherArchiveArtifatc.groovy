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
                                             def CREDENTIAL_ID = "jenkins-token"
                                             def SECRET = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance().getStore().getCredentials(com.cloudbees.plugins.credentials.domains.Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getSecret().getPlainText()
                                             def url = "https://"+ SECRET + "@sda.acaternberg.flow-training.beescloud.com/sb/job/ci-templates-demo/job/DEMO-ParameterUsage/job/initData/lastSuccessfulBuild/artifact/newparams.txt/*view*/"
                                             println url
                                             def result = ["/bin/bash", "-c", "curl -L " + url].execute().text.tokenize();
                                             println result
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
