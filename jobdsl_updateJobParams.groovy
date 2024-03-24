def myparam="$params"
// Split the string by comma
ArrayList elements = myparam.split(',')
// Create ArrayList and add elements
def arrayList = new ArrayList(elements)
println arrayList

pipelineJob('example-pipeline') {
    description('Updated by Job-DSL')
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