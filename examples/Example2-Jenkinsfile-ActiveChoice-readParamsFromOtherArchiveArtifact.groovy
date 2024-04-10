package examples

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
                                             import jenkins.model.*
                                             import com.cloudbees.plugins.credentials.domains.Domain
                                             import com.cloudbees.plugins.credentials.SystemCredentialsProvider
                                             def CREDENTIAL_ID = "jenkins-token"
                                             def cred = SystemCredentialsProvider.getInstance().getStore().getCredentials(Domain.global()).find { it.getId().equals(CREDENTIAL_ID) }.getSecret().getPlainText()
                                             def hostUrl = Jenkins.getInstance().getRootUrl().replace("https://","https://"+ cred + "@")
                                             def url = hostUrl + "job/ci-templates-demo/job/DEMO-ParameterUsage/job/initData/lastSuccessfulBuild/artifact/newparams.txt/*view*/"
                                             def result = ["/bin/bash", "-c", "curl -L " + url].execute().text.tokenize();
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
