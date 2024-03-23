def myparam="$params"
// Split the string by comma
ArrayList elements = myparam.split(',')
// Create ArrayList and add elements
def arrayList = new ArrayList(elements)
println arrayList
job('example-dsl-freestylejob') {
    parameters {
        choiceParam('OPTION',arrayList)
    }
    steps {
        shell("echo OPTION SELECTED: ${OPTION}")
    }
}


pipelineJob('example-dsl-pipelinejob') {
    parameters {
        choiceParam('OPTION',arrayList)
    }
    definition {
        //Jenkinsfile should better be in SCM but for demo purpose we make it here inline
        //see https://jenkinsci.github.io/job-dsl-plugin/#path/pipelineJob-definition-cpsScm-scm-git
        cps {
            script('''pipeline {
                agent none
                stages {
                    stage('selectedOption') {
                        steps {
                            echo "OPTION SELECTED: ${OPTION}"
                        }
                    }
                }
            }''')
        }
    }
}