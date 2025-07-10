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
                                                    // SHOULD NOT BE REQUIRED TO SET PROXY HERE SO IGNORE THE COMENTED LINES BELOW 
                                                    // USE THEM ONLY FOR TESTING/DEVELOPMENT PURPOSE
                                                    //def proxyHost = "squid-dev-proxy.squid.svc.cluster.local"
                                                    //def proxyPort = 3128                                                
                                                    // Set up the proxy
                                                    //System.setProperty("http.proxyHost", proxyHost)
                                                    //System.setProperty("http.proxyPort", proxyPort.toString())
                                                    //System.setProperty("https.proxyHost", proxyHost)
                                                    //System.setProperty("https.proxyPort", proxyPort.toString())                                                     
                                                    def url = new URL("https://api.github.com/repos/cb-ci-templates/ci-poc-params-update/branches")                                                   
                                                    def conn = url.openConnection()
                                                    // IF SECURED, SET YOUR TOKEN
                                                    //conn.setRequestProperty("Authorization", "token " + "<YOUR_GITHUB_TOKEN>")
                                                    def text = conn.inputStream.text
                                                    def branches = new groovy.json.JsonSlurper().parseText(text)*.name
                                                    println branches
                                                    return branches                                     
                                    ''']
                )
        )
        ]
)])
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
