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
                                 sandbox: false, script: '''
                                                def content=new URL ("https://raw.githubusercontent.com/cb-ci-templates/ci-poc-params-update/main/resources/choices.txt").getText()
                                                def values = []
                                                for(def line : content.split(',')) {
                                                    values.add(line.trim())
                                                }
                                                return values                                            ''']
                        )
                )
        ]
    ),
            [$class: 'JobLocalConfiguration', changeReasonComment: '']])
pipeline {
    agent any

    stages {
        stage('Hello') {
            steps {
                echo 'Hello World'
            }
        }
    }
}
