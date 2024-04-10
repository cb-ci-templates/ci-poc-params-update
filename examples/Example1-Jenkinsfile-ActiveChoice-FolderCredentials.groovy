properties([parameters(
        [activeChoice(choiceType: 'PT_SINGLE_SELECT',
                filterLength: 1, filterable: false,
                name: 'mychoice',
                randomName: 'choice-parameter-2381968702490339',
                script: groovyScript(
                        fallbackScript: [classpath: [],
                                         oldScript: '',
                                         sandbox  : false, script: 'return "false"'],
                        script: [classpath: [],
                                 oldScript: '',
                                 sandbox  : false, script: '''
                                        import jenkins.model.*
                                        import com.cloudbees.hudson.plugins.folder.*
                                        import com.cloudbees.plugins.credentials.*
                                        def folderName = 'ci-templates-demo'
                                        def credentialsID = "jenkins-token"
                                        AbstractFolder myFolder = Jenkins.instance.getAllItems(AbstractFolder.class).find{ (it.name == folderName) }
                                        def creds = CredentialsProvider.lookupCredentials(Credentials.class, myFolder)
                                        def cred=creds.find{(it.id == credentialsID)}
                                        def url = "https://"+ cred.getSecret().getPlainText() + "@example.com/sb/job/ci-templates-demo/job/DEMO-ParameterUsage/job/initData/lastSuccessfulBuild/artifact/newparams.txt/*view*/"
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
