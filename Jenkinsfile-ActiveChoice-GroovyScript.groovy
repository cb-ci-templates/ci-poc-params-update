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
return result                                       ''']
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
                  - name: git
                    image: bravissimolabs/alpine-git
                    command:
                    - cat
                    tty: true
                    workingDir: "/home/jenkins/agent"
                    securityContext:
                      runAsUser: 0               
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
        stage('UpdateParamsInRepo') {
            options {
                skipDefaultCheckout()
            }
            steps {
                container("git") {
                    git branch: 'main', changelog: false, credentialsId: 'github-user-ssh', poll: false, url: 'git@github.com:cb-ci-templates/ci-poc-params-update.git'
                    withCredentials([sshUserPrivateKey(credentialsId: 'github-user-ssh', keyFileVariable: 'CERT', usernameVariable: 'SSH_USER')]) {
                        sh """
                            mkdir -p ~/.ssh && chmod 700 ~/.ssh &&  cp -prf $CERT ~/.ssh/id_rsa && chmod 600 ~/.ssh/id_rsa
                            git config --global user.email \"acaternberg@cloudbees.com\"
                            git config --global user.name $SSH_USER
                            eval `ssh-agent -s`  && ssh-add ~/.ssh/id_rsa
                            ssh-keyscan -H github.com >> ~/.ssh/known_hosts
                            echo \$(date +\"%Y-%m-%d,%H:%M:%S\")  > resources/choices.txt
                            sleep 1
                            echo \$(date +\"%Y-%m-%d,%H:%M:%S\")  >> resources/choices.txt
                            git add resources/choices.txt
                            git commit -m \"update value\"
                            git push origin main
                         """
                    }
                }
            }
        }
    }
}
